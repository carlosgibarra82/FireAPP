package com.optic.fireapp.FCM;
import com.optic.fireapp.models.DataMessage;
import com.optic.fireapp.models.FCMResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAztcPXIY:APA91bFuHhRqVJBm4ilBxW2IZb_C5BIAVZlINBxr7QEj6KHHlS3NooHQFkHDYkEORbSodrFYBpgwUX0cy0x0gcRC2tcXB1JGS_mRvXXAx-wHW-kensQXMMBXx0PIhQk2Ti-elQWpPu6_"
    })
    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body DataMessage body);
}
