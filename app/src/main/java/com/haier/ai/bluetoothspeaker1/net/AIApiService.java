package com.haier.ai.bluetoothspeaker1.net;

import com.haier.ai.bluetoothspeaker1.Const;
import com.haier.ai.bluetoothspeaker1.bean.Oilprice.RequestOilprice;
import com.haier.ai.bluetoothspeaker1.bean.Oilprice.ResponseOilprice;
import com.haier.ai.bluetoothspeaker1.bean.calendar.RequestCalendar;
import com.haier.ai.bluetoothspeaker1.bean.calendar.ResponseCalendar;
import com.haier.ai.bluetoothspeaker1.bean.constellation.RequestConstellation;
import com.haier.ai.bluetoothspeaker1.bean.constellation.ResponseConstellation;
import com.haier.ai.bluetoothspeaker1.bean.holiday.RequestHoliday;
import com.haier.ai.bluetoothspeaker1.bean.holiday.ResponseHoliday;
import com.haier.ai.bluetoothspeaker1.bean.hotline.RequestHotline;
import com.haier.ai.bluetoothspeaker1.bean.hotline.ResponseHotline;
import com.haier.ai.bluetoothspeaker1.bean.limit.RequestLimit;
import com.haier.ai.bluetoothspeaker1.bean.limit.ResponseLimit;
import com.haier.ai.bluetoothspeaker1.bean.menu.RequestMenu;
import com.haier.ai.bluetoothspeaker1.bean.menu.ResponseMenu;
import com.haier.ai.bluetoothspeaker1.bean.movie.RequestMovie;
import com.haier.ai.bluetoothspeaker1.bean.movie.ResponseMovie;
import com.haier.ai.bluetoothspeaker1.bean.music.RequestMusic;
import com.haier.ai.bluetoothspeaker1.bean.music.ResponseMusic;
import com.haier.ai.bluetoothspeaker1.bean.news.RequestNews;
import com.haier.ai.bluetoothspeaker1.bean.news.ResponseNews;
import com.haier.ai.bluetoothspeaker1.bean.poetry.RequestPoetry;
import com.haier.ai.bluetoothspeaker1.bean.poetry.ResponsePoetry;
import com.haier.ai.bluetoothspeaker1.bean.stock.RequestStock;
import com.haier.ai.bluetoothspeaker1.bean.stock.ResponseStock;
import com.haier.ai.bluetoothspeaker1.bean.stock.ResponseStock1;
import com.haier.ai.bluetoothspeaker1.bean.translation.RequestTrans;
import com.haier.ai.bluetoothspeaker1.bean.translation.ResponseTrans;
import com.haier.ai.bluetoothspeaker1.bean.weather.RequestAqi;
import com.haier.ai.bluetoothspeaker1.bean.weather.RequestWeather;
import com.haier.ai.bluetoothspeaker1.bean.weather.ResponseAqi;
import com.haier.ai.bluetoothspeaker1.bean.weather.ResponseWeather;
import com.haier.ai.bluetoothspeaker1.bean.ximalaya.RequestXimalaya;
import com.haier.ai.bluetoothspeaker1.bean.ximalaya.ResponseXimalaya;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * author: qu
 * date: 17-1-13
 * introduce:
 */

public interface AIApiService {
    @POST(Const.URL_CONTENT)
    public Call<ResponseMusic> getMusicContent(@Header("accessToken") String accessToken,
                                               @Body RequestMusic requestMusic);

    /**
     * 获取新闻
     * @param accessToken
     * @param requestNews
     * @return
     */
    @POST(Const.URL_CONTENT)
    public Call<ResponseNews> getNewsContent(@Header("accessToken") String accessToken,
                                             @Body RequestNews requestNews);

    @POST(Const.URL_CONTENT)
    public Call<ResponseLimit> getLimitContent(@Header("accessToken") String accessToken,
                                               @Body RequestLimit requestLimit);

    @POST(Const.URL_CONTENT)
    public Call<ResponseOilprice> getOilPrice(@Header("accessToken") String accessToken,
                                              @Body RequestOilprice requestOilprice);

    @POST(Const.URL_CONTENT)
    public Call<ResponseStock> getStockInfo(@Header("accessToken") String accessToken,
                                           @Body RequestStock requestStock);

    @POST(Const.URL_CONTENT)
    public Call<ResponseTrans> getTransferResult(@Header("accessToken") String accessToken,
                                            @Body RequestTrans requestTrans);

    @POST(Const.URL_CONTENT)
    public Call<ResponseAqi> getAqiResult(@Header("accessToken") String accessToken,
                                               @Body RequestAqi requestAqi);

    @POST(Const.URL_CONTENT)
    public Call<ResponseMovie> getMovieInfo(@Header("accessToken") String accessToken,
                                            @Body RequestMovie requestMovie);

    @POST(Const.URL_CONTENT)
    public Call<ResponseHoliday> getHolidayInfo(@Header("accessToken") String accessToken,
                                              @Body RequestHoliday requestHoliday);

    @POST(Const.URL_CONTENT)
    public Call<ResponseHotline> getHotlineInfo(@Header("accessToken") String accessToken,
                                                @Body RequestHotline requestHotline);

    @POST(Const.URL_CONTENT)
    public Call<ResponseXimalaya> getXimalayaInfo(@Header("accessToken") String accessToken,
                                                 @Body RequestXimalaya requestXimalaya);

    @POST(Const.URL_CONTENT)
    public Call<ResponseConstellation> getConstellation(@Header("accessToken") String accessToken,
                                                       @Body RequestConstellation requestConstellation);

    @POST(Const.URL_CONTENT)
    public Call<ResponseCalendar> getCalendarInfo(@Header("accessToken") String accessToken,
                                                   @Body RequestCalendar requestCalendar);

    @POST(Const.URL_CONTENT)
    public Call<ResponseStock1> getStockInfo1(@Header("accessToken") String accessToken,
                                              @Body RequestStock requestStock);

    @POST(Const.URL_CONTENT)
    public Call<ResponseWeather> getWeatherInfo(@Header("accessToken") String accessToken,
                                               @Body RequestWeather requestWeather);

    @POST(Const.URL_CONTENT)
    public Call<ResponseMenu> getCookInfo(@Header("accessToken") String accessToken,
                                             @Body RequestMenu requestMenu);

    @POST(Const.URL_CONTENT)
    public Call<ResponsePoetry> getPoetry(@Header("accessToken") String accessToken,
                                            @Body RequestPoetry requestPoetry);
}
