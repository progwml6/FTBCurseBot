package com.feed_the_beast.ftbcurseappbot.utils;

import lombok.Getter;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * Created by progwml6 on 8/16/16.
 */
public class NetworkingUtils {
    private NetworkingUtils () {

    }

    @Getter
    private static final OkHttpClient okHttpClient = new OkHttpClient();
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";

    public static String getSynchronous (String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = okHttpClient.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }

        Headers responseHeaders = response.headers();

        return response.body().string();
    }
}
