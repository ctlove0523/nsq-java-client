package io.github.ctlove0523.nsq;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
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

    public String toJson() {
        return new Gson().toJson(this);
    }
}
