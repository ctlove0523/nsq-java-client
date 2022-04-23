package io.github.ctlove0523.nsq;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public class ClientMetadata {
    @SerializedName("client_id")
    private String clientId;

    private String hostname;

    @SerializedName("feature_negotiation")
    private Boolean featureNegotiation;

    @SerializedName("heartbeat_interval")
    private Integer heartbeatInterval;

    @SerializedName("output_buffer_size")
    private Integer outputBufferSize;

    @SerializedName("output_buffer_timeout")
    private Integer outputBufferTimeout;

    @SerializedName("tls_v1")
    private Boolean tlsV1;

    @SerializedName("snappy")
    private Boolean snappy;

    @SerializedName("deflate")
    private Boolean deflate;

    @SerializedName("deflate_level")
    private Integer deflateLevel;

    @SerializedName("sample_rate")
    private Integer sampleRate;

    @SerializedName("user_agent")
    private String userAgent;

    @SerializedName("msg_timeout")
    private Integer msgTimeout;

    public ClientMetadata() {

    }

    public ClientMetadata(Builder builder) {
        this.clientId = builder.clientId;
        this.hostname = builder.hostname;
        this.featureNegotiation = builder.featureNegotiation;
        this.heartbeatInterval = builder.heartbeatInterval;
        this.outputBufferSize = builder.outputBufferSize;
        this.outputBufferTimeout = builder.outputBufferTimeout;
        this.tlsV1 = builder.tlsV1;
        this.deflate = builder.deflate;
        this.snappy = builder.snappy;
        this.userAgent = builder.userAgent;
        this.deflateLevel = builder.deflateLevel;
        this.sampleRate = builder.sampleRate;
        this.msgTimeout = builder.msgTimeout;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static class Builder {
        private String clientId;
        private String hostname;
        private Boolean featureNegotiation;
        private Integer heartbeatInterval;
        private Integer outputBufferSize;
        private Integer outputBufferTimeout;
        private Boolean tlsV1;
        private Boolean deflate;
        private Boolean snappy;
        private String userAgent;
        private Integer deflateLevel;
        private Integer sampleRate;
        private Integer msgTimeout;

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder hostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public Builder featureNegotiation(Boolean featureNegotiation) {
            this.featureNegotiation = featureNegotiation;
            return this;
        }

        public Builder heartbeatInterval(int heartbeatInterval) {
            if (heartbeatInterval < 1000 || heartbeatInterval > 60 * 1000) {
                throw new IllegalArgumentException("heartbeat_interval must between[1000,60000]");
            }
            this.heartbeatInterval = heartbeatInterval;
            return this;
        }

        public Builder outputBufferSize(int outputBufferSize) {
            this.outputBufferSize = outputBufferSize;
            return this;
        }

        public Builder outputBufferTimeout(int outputBufferTimeout) {
            this.outputBufferTimeout = outputBufferTimeout;
            return this;
        }

        public Builder tlsV1(Boolean tlsV1) {
            this.tlsV1 = tlsV1;
            return this;
        }

        public Builder snappy(boolean snappy) {
            this.snappy = snappy;
            return this;
        }

        public Builder deflate(boolean deflate) {
            this.deflate = deflate;
            return this;
        }

        public Builder deflateLevel(int deflateLevel) {
            this.deflateLevel = deflateLevel;
            return this;
        }

        public Builder sampleRate(int sampleRate) {
            this.sampleRate = sampleRate;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder msgTimeout(int msgTimeout) {
            this.msgTimeout = msgTimeout;
            return this;
        }

        public ClientMetadata build() {
            return new ClientMetadata(this);
        }
    }
}
