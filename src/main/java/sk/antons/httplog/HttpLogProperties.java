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


public class HttpLogProperties {

    private String requestCondition;
    private String responseCondition;

    private String logger;
    private String formatter;

    private String requestStartPrefix;
    private String requestPrefix;
    private String responsePrefix;

    private Boolean identity = false;
    private Boolean remoteHost = false;
    private Boolean remoteAddr = false;
    private Boolean protocol = false;
    private Boolean doNothing = false;

    private String requestHeaders;
    private String responseHeaders;
    private Formatter requestPayload;
    private Formatter responsePayload;

    private Integer expectedRequestLength;
    private Integer expectedResponseLength;

    public String getRequestCondition() { return requestCondition; }
    public void setRequestCondition(String requestCondition) { this.requestCondition = requestCondition; }
    public String getResponseCondition() { return responseCondition; }
    public void setResponseCondition(String responseCondition) { this.responseCondition = responseCondition; }
    public String getLogger() { return logger; }
    public void setLogger(String logger) { this.logger = logger; }
    public String getRequestStartPrefix() { return requestStartPrefix; }
    public void setRequestStartPrefix(String requestStartPrefix) { this.requestStartPrefix = requestStartPrefix; }
    public String getRequestPrefix() { return requestPrefix; }
    public void setRequestPrefix(String requestPrefix) { this.requestPrefix = requestPrefix; }
    public String getResponsePrefix() { return responsePrefix; }
    public void setResponsePrefix(String responsePrefix) { this.responsePrefix = responsePrefix; }
    public Boolean getIdentity() { return identity; }
    public void setIdentity(Boolean identity) { this.identity = identity; }
    public Boolean getRemoteHost() { return remoteHost; }
    public void setRemoteHost(Boolean remoteHost) { this.remoteHost = remoteHost; }
    public Boolean getRemoteAddr() { return remoteAddr; }
    public void setRemoteAddr(Boolean remoteAddr) { this.remoteAddr = remoteAddr; }
    public Boolean getProtocol() { return protocol; }
    public void setProtocol(Boolean protocol) { this.protocol = protocol; }
    public Boolean getDoNothing() { return doNothing; }
    public void setDoNothing(Boolean doNothing) { this.doNothing = doNothing; }
    public String getRequestHeaders() { return requestHeaders; }
    public void setRequestHeaders(String requestHeaders) { this.requestHeaders = requestHeaders; }
    public String getResponseHeaders() { return responseHeaders; }
    public void setResponseHeaders(String responseHeaders) { this.responseHeaders = responseHeaders; }
    public Formatter getRequestPayload() { return requestPayload; }
    public void setRequestPayload(Formatter requestPayload) { this.requestPayload = requestPayload; }
    public Formatter getResponsePayload() { return responsePayload; }
    public void setResponsePayload(Formatter responsePayload) { this.responsePayload = responsePayload; }
    public Integer getExpectedRequestLength() { return expectedRequestLength; }
    public void setExpectedRequestLength(Integer expectedRequestLength) { this.expectedRequestLength = expectedRequestLength; }
    public Integer getExpectedResponseLength() { return expectedResponseLength; }
    public void setExpectedResponseLength(Integer expectedResponseLength) { this.expectedResponseLength = expectedResponseLength; }
    public String getFormatter() { return formatter; }
    public void setFormatter(String formatter) { this.formatter = formatter; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("HttpLogProperties :");
        if(requestCondition != null) sb.append("\nrequestCondition=").append(requestCondition);
        if(responseCondition != null) sb.append("\nresponseCondition=").append(responseCondition);
        if(logger != null) sb.append("\nlogger=").append(logger);
        if(requestStartPrefix != null) sb.append("\nrequestStartPrefix=").append(requestStartPrefix);
        if(requestPrefix != null) sb.append("\nrequestPrefix=").append(requestPrefix);
        if(responsePrefix != null) sb.append("\nresponsePrefix=").append(responsePrefix);
        if(identity != null) sb.append("\nidentity=").append(identity);
        if(remoteHost != null) sb.append("\nremoteHost=").append(remoteHost);
        if(remoteAddr != null) sb.append("\nremoteAddr=").append(remoteAddr);
        if(protocol != null) sb.append("\nprotocol=").append(protocol);
        if(doNothing != null) sb.append("\ndoNothing=").append(doNothing);
        if(requestHeaders != null) sb.append("\nrequestHeaders=").append(requestHeaders);
        if(responseHeaders != null) sb.append("\nresponseHeaders=").append(responseHeaders);
        if(requestPayload != null) sb.append("\nrequestPayload=").append(requestPayload);
        if(responsePayload != null) sb.append("\nresponsePayload=").append(responsePayload);
        if(expectedRequestLength != null) sb.append("\nexpectedRequestLength=").append(expectedRequestLength);
        if(expectedResponseLength != null) sb.append("\nexpectedResponseLength=").append(expectedResponseLength);
        return sb.toString();
    }

    public boolean isEmpty() {
        if(requestCondition != null) return false;
        if(responseCondition != null) return false;
        if(logger != null) return false;
        if(requestStartPrefix != null) return false;
        if(requestPrefix != null) return false;
        if(responsePrefix != null) return false;
        if(identity != null) return false;
        if(remoteHost != null) return false;
        if(remoteAddr != null) return false;
        if(protocol != null) return false;
        if(doNothing != null) return false;
        if(requestHeaders != null) return false;
        if(responseHeaders != null) return false;
        if(requestPayload != null) return false;
        if(responsePayload != null) return false;
        if(expectedRequestLength != null) return false;
        if(expectedResponseLength != null) return false;
        return true;
    }

    public static class Formatter {

        private String type;
        private String encoding;
        private String indent;
        private Boolean forceOneLine;
        private Integer cutStringLiterals;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getEncoding() { return encoding; }
        public void setEncoding(String encoding) { this.encoding = encoding; }
        public String getIndent() { return indent; }
        public void setIndent(String indent) { this.indent = indent; }
        public Boolean getForceOneLine() { return forceOneLine; }
        public void setForceOneLine(Boolean forceOneLine) { this.forceOneLine = forceOneLine; }
        public Integer getCutStringLiterals() { return cutStringLiterals; }
        public void setCutStringLiterals(Integer cutStringLiterals) { this.cutStringLiterals = cutStringLiterals; }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Formatter{");
            sb.append("type=").append(type);
            if(encoding != null) sb.append(", encoding=").append(encoding);
            if(indent != null) sb.append(", indent=").append(indent);
            if(forceOneLine != null) sb.append(", forceOneLine=").append(forceOneLine);
            if(cutStringLiterals != null) sb.append(", cutStringLiterals=").append(cutStringLiterals);
            sb.append('}');
            return sb.toString();
        }

        public boolean isEmpty() {
            if(encoding != null) return false;
            if(indent != null) return false;
            if(forceOneLine != null) return false;
            if(cutStringLiterals != null) return false;
            return true;
        }
    }
}
