/*
 * Copyright 2025 Anton Straka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sk.antons.httplog;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import sk.antons.httplog.HttpLogProperties.Formatter;
import sk.antons.servlet.filter.LogFilter;
import sk.antons.servlet.filter.builder.FilterConfBuilder;
import sk.antons.servlet.filter.builder.FilterConfSelectorBuilder;
import sk.antons.servlet.filter.builder.LogFilterBuilder;
import sk.antons.servlet.filter.builder.RequestConditionBuilder;
import sk.antons.servlet.filter.builder.ResponseConditionBuilder;
import sk.antons.servlet.filter.builder.StringConditionBuilder;
import sk.antons.servlet.filter.formatter.MultiLineFormatter;
import sk.antons.servlet.filter.formatter.OneLineFormatter;

@ConditionalOnProperty(
    value="httplog.enabled",
    havingValue = "true",
    matchIfMissing = true)
@Configuration
public class HttpLogConfiguration {
    private static Logger log = LoggerFactory.getLogger(HttpLogConfiguration.class);


    @Bean @ConditionalOnMissingBean(LogFilter.class)
    public FilterRegistrationBean httpLogFilterRegistrationBean(Environment env) {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        LogFilter filter = httpLog(env);
        registrationBean.setFilter(filter);
        registrationBean.setOrder(Integer.MAX_VALUE); // mostly because of auth filter (but some response header are not visible)
        return registrationBean;
    }


    private LogFilter httpLog(Environment env) {
//        for(Iterator it = ((AbstractEnvironment) env).getPropertySources().iterator(); it.hasNext(); ) {
//            PropertySource propertySource = (PropertySource) it.next();
//            if (propertySource instanceof MapPropertySource) {
//                Map<String, Object> map = ((MapPropertySource) propertySource).getSource();
//                for(Map.Entry<String, Object> entry : map.entrySet()) {
//                    String key = entry.getKey();
//                    if(!key.startsWith("httplog.")) continue;
//                    String value = env.getProperty(key);
//                    System.out.println(key + ": " + value);
//                }
//            }
//        }
        HttpLogProperties defaultProps = reqolve(env, "httplog.");
        List<HttpLogProperties> cases = new ArrayList<>();
        int counter = 0;
        String reqCond = env.getProperty("httplog.case["+counter+"].condition.request");
        String resCond = env.getProperty("httplog.case["+counter+"].condition.response");
        while((reqCond != null) || (resCond != null)) {
            HttpLogProperties props = reqolve(env, "httplog.case["+counter+"].");
            if(props != null) cases.add(props);
            counter++;
            reqCond = env.getProperty("httplog.case["+counter+"].condition.request");
            resCond = env.getProperty("httplog.case["+counter+"].condition.response");
        }
        if(defaultProps == null) defaultProps = new HttpLogProperties();
        if(cases.isEmpty()) {
            HttpLogProperties p = new HttpLogProperties();
            p.setRequestCondition("any");
            cases.add(p);
        }

        LogFilterBuilder builder = LogFilter.builder();

        FilterConfBuilder<LogFilterBuilder> dbuilder = builder.defaultConf();
        if(defaultProps.getLogger() == null) defaultProps.setLogger("httplog");
        fillConf(dbuilder, defaultProps);
        dbuilder.done();

        for(HttpLogProperties aCase : cases) {
            FilterConfSelectorBuilder<LogFilterBuilder> sbuilder = builder.inCase();
            if(aCase.getRequestCondition() != null) {
                RequestConditionBuilder<?> cbuilder = sbuilder.request();
                StringConditionBuilder<?, HttpServletRequest> scbuilder = null;
                String condition = aCase.getRequestCondition();
                int index = 0;
                ConditionToken token = ConditionToken.next(condition, index);
                while(token != null) {
                    if(scbuilder != null) {
                        fillStringCondition(scbuilder, token);
                        scbuilder = null;
                    } else if("{".equals(token.name)) {
                        cbuilder.lb();
                    } else if("}".equals(token.name)) {
                        cbuilder.rb();
                    } else if("or".equalsIgnoreCase(token.name)) {
                        cbuilder.or();
                    } else if("not".equalsIgnoreCase(token.name)) {
                        cbuilder.not();
                    } else if("and".equalsIgnoreCase(token.name)) {
                        cbuilder.and();
                    } else if("any".equalsIgnoreCase(token.name)) {
                        cbuilder.any();
                    } else if("contentType".equals(token.name)) {
                        scbuilder = cbuilder.contentType();
                    } else if("contextPath".equals(token.name)) {
                        scbuilder = cbuilder.contextPath();
                    } else if("header".equals(token.name)) {
                        scbuilder = cbuilder.header(token.parameter);
                    } else if("localAddr".equals(token.name)) {
                        scbuilder = cbuilder.localAddr();
                    } else if("localName".equals(token.name)) {
                        scbuilder = cbuilder.localName();
                    } else if("method".equals(token.name)) {
                        scbuilder = cbuilder.method();
                    } else if("param".equals(token.name)) {
                        scbuilder = cbuilder.param(token.parameter);
                    } else if("path".equals(token.name)) {
                        scbuilder = cbuilder.path();
                    } else if("pathInfo".equals(token.name)) {
                        scbuilder = cbuilder.pathInfo();
                    } else if("pathTranslated".equals(token.name)) {
                        scbuilder = cbuilder.pathTranslated();
                    } else if("queryString".equals(token.name)) {
                        scbuilder = cbuilder.queryString();
                    } else if("remoteAddr".equals(token.name)) {
                        scbuilder = cbuilder.remoteAddr();
                    } else if("remoteHost".equals(token.name)) {
                        scbuilder = cbuilder.remoteHost();
                    } else if("remoteUser".equals(token.name)) {
                        scbuilder = cbuilder.remoteUser();
                    } else if("schema".equals(token.name)) {
                        scbuilder = cbuilder.schema();
                    } else if("servletPath".equals(token.name)) {
                        scbuilder = cbuilder.servletPath();
                    } else if("uri".equals(token.name)) {
                        scbuilder = cbuilder.uri();
                    } else {
                        throw new IllegalArgumentException("unknown condition token " + token + " at " + condition) ;
                    }

                    index = token.endpos;
                    token = ConditionToken.next(condition, index);
                }
                cbuilder.done();
            }
            if(aCase.getResponseCondition() != null) {
                ResponseConditionBuilder<?> cbuilder = sbuilder.response();
                StringConditionBuilder<?, HttpServletResponse> scbuilder = null;
                String condition = aCase.getResponseCondition();
                int index = 0;
                ConditionToken token = ConditionToken.next(condition, index);
                while(token != null) {
                    if(scbuilder != null) {
                        fillStringCondition(scbuilder, token);
                        scbuilder = null;
                    } else if("{".equals(token.name)) {
                        cbuilder.lb();
                    } else if("}".equals(token.name)) {
                        cbuilder.rb();
                    } else if("not".equalsIgnoreCase(token.name)) {
                        cbuilder.not();
                    } else if("or".equalsIgnoreCase(token.name)) {
                        cbuilder.or();
                    } else if("and".equalsIgnoreCase(token.name)) {
                        cbuilder.and();
                    } else if("any".equalsIgnoreCase(token.name)) {
                        cbuilder.any();
                    } else if("statusOK".equals(token.name)) {
                        cbuilder.statusOK();
                    } else if("contentType".equals(token.name)) {
                        scbuilder = cbuilder.contentType();
                    } else if("status".equals(token.name)) {
                        scbuilder = cbuilder.status();
                    } else if("header".equals(token.name)) {
                        scbuilder = cbuilder.header(token.parameter);
                    } else {
                        throw new IllegalArgumentException("unknown condition token " + token + " at " + condition) ;
                    }

                    index = token.endpos;
                    token = ConditionToken.next(condition, index);
                }
                cbuilder.done();
            }

            FilterConfBuilder<?> confbuilder = sbuilder.conf();
            fillConf(confbuilder, aCase);
            confbuilder.done();

            sbuilder.done();
        }

        LogFilter filter = builder.build();

        log.debug("httplog conf: {}", filter.configurationInfo());

        return filter;
    }


    private static void fillStringCondition(StringConditionBuilder<?, ?> builder, ConditionToken token) {
        if("contains".equals(token.name)) {
            builder.contains(token.parameter);
        } else if("endsWith".equals(token.name)) {
            builder.endsWitn(token.parameter);
        } else if("equals".equals(token.name)) {
            builder.equals(token.parameter);
        } else if("equalsIgnoreCase".equals(token.name)) {
            builder.equalsIgnoreCase(token.parameter);
        } else if("exists".equals(token.name)) {
            builder.exists();
        } else if("match".equals(token.name)) {
            builder.match(token.parameter);
        } else if("regexp".equals(token.name)) {
            builder.regexp(token.parameter);
        } else if("startsWith".equals(token.name)) {
            builder.startsWith(token.parameter);
        } else {
            throw new IllegalArgumentException("unknown string condition token " + token ) ;
        }
    }

    private static void fillConf(FilterConfBuilder<?> builder, HttpLogProperties props) {
        if(props.getLogger() != null) {
            String logger = props.getLogger();
            String level = "debug";
            int pos = logger.indexOf(':');
            if(pos > -1) {
                level = logger.substring(pos+1);
                logger = logger.substring(0, pos);
            }
            final Logger hlog = LoggerFactory.getLogger(logger);
            if("info".equals(level)) {
                builder.messageConsumer(message -> hlog.info(message));
                builder.messageConsumerEnabled(() -> hlog.isInfoEnabled());
            } else if("trace".equals(level)) {
                builder.messageConsumer(message -> hlog.trace(message));
                builder.messageConsumerEnabled(() -> hlog.isTraceEnabled());
            } else if("warn".equals(level)) {
                builder.messageConsumer(message -> hlog.warn(message));
                builder.messageConsumerEnabled(() -> hlog.isWarnEnabled());
            } else if("error".equals(level)) {
                builder.messageConsumer(message -> hlog.error(message));
                builder.messageConsumerEnabled(() -> hlog.isErrorEnabled());
            } else {
                builder.messageConsumer(message -> hlog.debug(message));
                builder.messageConsumerEnabled(() -> hlog.isDebugEnabled());
            }
        }
        if(props.getDoNothing() != null) builder.doNothing(props.getDoNothing());
        if(props.getIdentity() != null) builder.identity(props.getIdentity());
        if(props.getProtocol() != null) builder.protocol(props.getProtocol());
        if(props.getRemoteAddr() != null) builder.remoteAddr(props.getRemoteAddr());
        if(props.getRemoteHost() != null) builder.remoteHost(props.getRemoteHost());
        if(props.getExpectedRequestLength() != null) builder.expectedResponseLength(props.getExpectedRequestLength());
        if(props.getExpectedResponseLength() != null) builder.expectedResponseLength(props.getExpectedResponseLength());
        if(props.getRequestHeaders() != null) builder.requestHeaderFilter(headers(props.getRequestHeaders()));
        if(props.getRequestPayload() != null) builder.requestPayloadFormatter(payload(props.getRequestPayload()));
        if(props.getRequestPrefix() != null) builder.requestPrefix(notempty(props.getRequestPrefix()));
        if(props.getRequestStartPrefix() != null) builder.requestStartPrefix(notempty(props.getRequestStartPrefix()));
        if(props.getResponseHeaders() != null) builder.responseHeaderFilter(headers(props.getResponseHeaders()));
        if(props.getResponsePayload() != null) builder.responsePayloadFormatter(payload(props.getResponsePayload()));
        if(props.getResponsePrefix() != null) builder.responsePrefix(notempty(props.getResponsePrefix()));
        if(props.getFormatter() != null) {
            if("multiline".equals(props.getFormatter())) builder.formatter(MultiLineFormatter.Factory.instance());
            else if("oneline".equals(props.getFormatter())) builder.formatter(OneLineFormatter.Factory.instance());
        }

    }

    private static String notempty(String value) {
        if(value == null) return null;
        if(value.trim().length() == 0) return null;
        return value;
    }

    private static Function<InputStream, String> payload(Formatter value) {
        if(value == null) return null;
        if(value.isEmpty()) return null;
        if("json".equals(value.getType())) {
            LogFilter.Body.Json fmt = LogFilter.Body.Json.instance();
            if(value.getEncoding() != null) fmt.encoding(value.getEncoding());
            if(value.getCutStringLiterals() != null) fmt.cutStringLiterals(value.getCutStringLiterals());
            if(value.getForceOneLine() != null) fmt.forceOneLine(value.getForceOneLine());
            if(value.getIndent() != null) fmt.indent(value.getIndent());
            return fmt.format();
        } else if("xml".equals(value.getType())) {
            LogFilter.Body.Xml fmt = LogFilter.Body.Xml.instance();
            if(value.getEncoding() != null) fmt.encoding(value.getEncoding());
            if(value.getCutStringLiterals() != null) fmt.cutStringLiterals(value.getCutStringLiterals());
            if(value.getForceOneLine() != null) fmt.forceOneLine(value.getForceOneLine());
            if(value.getIndent() != null) fmt.indent(value.getIndent());
            return fmt.format();
        } else {
            LogFilter.Body.AsIs fmt = LogFilter.Body.AsIs.instance();
            if(value.getEncoding() != null) fmt.encoding(value.getEncoding());
            if(value.getCutStringLiterals() != null) fmt.cutTo(value.getCutStringLiterals());
            if(value.getForceOneLine() != null) fmt.forceOneLine(value.getForceOneLine());
            return fmt.format();
        }
    }

    private static Predicate<String> headers(String value) {
        if(value == null) return null;
        value = value.trim();
        if(value.length() == 0) return null;
        if("*".equals(value)) return LogFilter.Headers.all();
        String[] split = value.split(",");
        return LogFilter.Headers.listed(split);
    }

    private static HttpLogProperties reqolve(Environment env, String prefix) {
        HttpLogProperties props  = new HttpLogProperties();
        props.setDoNothing(boolprop(env, prefix + "doNothing"));
        props.setLogger(prop(env, prefix + "logger"));
        props.setFormatter(prop(env, prefix + "formatter"));

        props.setExpectedRequestLength(intprop(env, prefix + "request.expectedLength"));
        props.setExpectedResponseLength(intprop(env, prefix + "response.expectedLength"));
        props.setIdentity(boolprop(env, prefix + "request.identity"));
        props.setProtocol(boolprop(env, prefix + "request.protocol"));
        props.setRemoteAddr(boolprop(env, prefix + "request.remoteAddr"));
        props.setRemoteHost(boolprop(env, prefix + "request.remoteHost"));
        props.setRequestCondition(prop(env, prefix + "condition.request"));
        props.setRequestHeaders(prop(env, prefix + "request.headers"));
        props.setRequestPayload(reqolveFormatter(env, prefix+"request.payload."));
        props.setRequestPrefix(prop(env, prefix + "request.prefix"));
        props.setRequestStartPrefix(prop(env, prefix + "request.startPrefix"));
        props.setResponseCondition(prop(env, prefix + "condition.response"));
        props.setResponseHeaders(prop(env, prefix + "response.headers"));
        props.setResponsePayload(reqolveFormatter(env, prefix+"response.payload."));
        props.setResponsePrefix(prop(env, prefix + "response.prefix"));

        if(props.isEmpty()) return null;
        return props;
    }

    private static HttpLogProperties.Formatter reqolveFormatter(Environment env, String prefix) {
        HttpLogProperties.Formatter frmt  = new HttpLogProperties.Formatter();
        frmt.setCutStringLiterals(intprop(env, prefix + "cutStringLiterals"));
        frmt.setEncoding(prop(env, prefix + "encoding"));
        frmt.setForceOneLine(boolprop(env, prefix + "forceOneLine"));
        frmt.setIndent(prop(env, prefix + "indent"));
        frmt.setType(prop(env, prefix + "type"));
        if(frmt.isEmpty()) return null;
        return frmt;
    }

    private static String prop(Environment env, String name) {
        return env.getProperty(name);
    }

    private static Integer intprop(Environment env, String name) {
        String value = prop(env, name);
        if(value == null) return null;
        value = value.trim();
        if(value.length() == 0) return null;
        try {
            return Integer.parseInt(value);
        } catch(Exception e) {
            throw new IllegalArgumentException("unable to parse property " + name + " as int from " + value);
        }
    }

    private static Boolean boolprop(Environment env, String name) {
        String value = prop(env, name);
        if(value == null) return null;
        value = value.trim();
        if(value.length() == 0) return null;
        try {
            return Boolean.parseBoolean(value);
        } catch(Exception e) {
            throw new IllegalArgumentException("unable to parse property " + name + " as bool from " + value);
        }
    }

    private static class ConditionToken {

        int startpos = -1;
        int endpos = -1;
        String value;
        String name;
        String parameter;

        public static ConditionToken next(String text, int pos) {
            if(text == null) return null;
            if(pos < 0) pos = 0;
            int len = text.length();
            int index = pos;
            if(len <= index ) return null;
            //skip spaces
            while((index < len) && isSpace(text.charAt(index))) index++;
            if(len <= index ) return null;
            ConditionToken next = new ConditionToken();
            next.startpos = index;
            while((index < len) && (!isTokenEnd(text.charAt(index)))) index++;
            next.name = text.substring(next.startpos, index);
            if((index < len) && (text.charAt(index) == '(')) {
                //find right bracket
                StringBuilder sb = new StringBuilder();
                boolean escape = false;
                int lbpos = index;
                index++;
                while(index < len) {
                    char c = text.charAt(index);
                    if(escape) {
                        if(c == ')') sb.append(c);
                        else if(c == 'n') sb.append('\n');
                        else if(c == 't') sb.append('\t');
                        else if(c == '\\') sb.append('\\');
                        else throw new IllegalArgumentException("invalid escape sequence ad position " + index + " in text " + text);
                        escape = false;
                    } else {
                        if(c == '\\') {
                            escape = true;
                        } else if(c == ')') {
                            next.endpos = index+1;
                            next.parameter = sb.toString();
                            break;
                        } else {
                            sb.append(c);
                        }
                    }
                    index++;
                }
                if(index>=len) throw new IllegalArgumentException("no matching bracket at index " + lbpos + " in " + text);
            } else {
                next.endpos = index;
            }

            return next;
        }

        private static boolean isSpace(char c) {
            if(c == ' ') return true;
            else if(c == '.') return true;
            else if(c == '\t') return true;
            else if(c == '\n') return true;
            else return false;
        }

        private static boolean isTokenEnd(char c) {
            if(isSpace(c)) return true;
            else if(c == '(') return true;
            else return false;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(name);
            if(parameter != null) sb.append('(').append(parameter).append(')');
            return sb.toString();
        }
    }

}
