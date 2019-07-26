package com.wdb.pdd.javaapi;

import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.pdd.pop.sdk.http.PopHttpClient;
import com.pdd.pop.sdk.http.api.request.PddGoodsListGetRequest;
import com.pdd.pop.sdk.http.api.response.PddGoodsListGetResponse;
import com.wdb.pdd.api.pojo.entity.ReguserDO;
import com.wdb.pdd.api.service.batch.IGoodsBatchBizService;
import com.wdb.pdd.api.service.sys.IReguserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JavaApiApplicationTests {

    @Autowired
    private PopHttpClient popHttpClient;
    @Autowired
    private IReguserService reguserService;
    @Autowired
    private IGoodsBatchBizService goodsBatchBizService;
    /*@Resource(name = "processJob")
    private Job processJob;
    @Autowired
    private JobLauncher jobLauncher;*/

    @Test
    public void test(){
        System.out.println(Base64Decoder.decodeStr("6IO95aSf55yL5Yiw6L+Z5q616K+d55qE5YWE5byf6K+B5piO5L2g6I" +
                "O95aSf5YGa5Yiw5Y2V5YWD5rWL6K+V77yM5q+U5oiR55qE5Lmg5oOv5aW95aSa5LqG77yM5oiR5LiA6Iis5LiN5Zac5qyi5YGa5Y2V5YWD5rW" +
                "L6K+V55qECuaOpeS4i+adpeimgeWBmuWNleWVhuWTgee8lui+kSDlsLHmmK/osIPmi7zlpJrlpJrnmoTllYblk4HnvJbovpHmjqXlj6MK5Y+" +
                "m5aSW5YiX6KGo5Lit5pyJ5Y2V5ZWG5ZOB55qE5ZCN56ew55u05o6l5L+u5pS5IOWNleWVhuWTgeeahOWQhOS4qnNrdeS7t+agvC/lupPlrZj" +
                "kv67mlLkK5Lul5LiK5Lik5Liq5b6F5a6M5oiQ55qE6YO95Zyo5om55aSE55CG5Lit5a6e546w6L+H77yM5Y+q5LiN6L+H5LiN5YaN5piv5by" +
                "C5q2l57q/56iL6LCD55So77yM6ICM5piv5YmN56uv55So5oi3562J5b6F5ZCM5q2l57q/56iL5omn6KGM5a6M5q+VCui/mei+uei/mOaYr+a" +
                "cieW+heWVhuamt+eahO+8jOWboOS4uuacieWPr+iDveS4gOS4quWVhuWTgeacieWlveWkmueahHNrdeS9huaYr3NrdeW6k+WtmOi/mei+ueS" +
                "/ruaUueaYr+WNleeLrOS4gOS4qnNrdeS4gOS4qnNrdeeahOiwg+eUqOeahO+8jArllYblk4HnvJbovpHmjqXlj6PkuK3kv67mlLnlupPlrZ" +
                "jkuI3og73nm7TmjqXopobnm5blhpnvvIzogIzmmK/lj6rog73mraPotJ/mlbDliqDlh48K5ZWG5ZOB55qE5qih5Z2X5a6M5LqG77yM5bCx" +
                "5piv5ZWG5ZOB6YeH6ZuG6L+Z5Z2X55qE6ZyA5rGC5LqG77yM5oiR5Lmf5YaZ5LqG54K55Lic6KW/77yM5Zyo6L+Z5Liq6aG555uu5bel56" +
                "iL55qE5ZCM57qn55uu5b2V5Lit77yM5L2G5piv5rKh5pyJ5a6e6LSo55qE5Lic6KW/77yMCueUqOeahOaYr3dlYm1hZ2ljLOa3mOWuneea" +
                "hOWVhuWTgeaVsOaNruWFqOWcqGpz5Lit77yM5Y+q5pyJ6K+m5oOF6aG15Zu+54mH6ZyA6KaB5LqM5qyh6K+35rGC6I635Y+W77yM5L2G5p" +
                "iv5aW95Zyo6L+Z5Liq5LqM5qyh6K+35rGC6L+e5o6l5rKh5pyJ6ZmQ5Yi25b+F6aG75b2T5YmN6aG16K6/6Zeu77yMCuS9huaYr+Wunum" +
                "ZhemUgOWUruS7t+agvOi/meS4qumcgOimgei/meS6m++8jOaJgOS7peWQjOihjOayoeaciei/m+ihjOi/meS4quaVsOaNrueahOWkhOe" +
                "Qhgrmi7zlpJrlpJrnmoTllYblk4HmlbDmja7lvojlpb3lj5blh7rvvIzlrozlhajmmK/lnKhqc+iEmuacrOS4reW3sue7j+e7hOe7h+W" +
                "lveeahGpzb27kuLLvvIzpgbXku47mi7zlpJrlpJrnmoTnu5PmnoQK55yL5LqG5Yeg5aSp77yM6L+Z5LiA5Z2X6KaB5piv5Y2V54us5YGa5" +
                "aW95aSa6Zeu6aKY6L+Y5piv6KaB6ICD6JmR55qE77yM5omA5Lul6LCD56CU5LqG5LiA5LiL5o+Q5L6b6L+Z5Lqb5pWw5o2u55qE5Y6C5Z" +
                "WG77yM5aSn5qaC5LiA5a6255S15ZWG55qE5pWw5o2u5ZyoNTAw5YWD5LiA5Liq5pyI55qE5qC35a2Q77yMCuaJgOS7peWmguaenOWJje" +
                "acn+mcgOaxgumHj+S4iuavlOi+g+Wwj++8jOW5tuWPkeS4jemrmOaIluiAheivtOezu+e7n+mZkOWItuS6huavj+S4quS6uueahOiwg+" +
                "eUqOmHj+eahOWfuuehgOS4iu+8jOW6lOWvueWkp+WOgueahOWPjeeIrOiZq+acuuWItui/mOaYr+ayoemXrumimOeahOWwseiHquW3s" +
                "eWOu+mHh+mbhuWQpwrmiJHmnaXov5novrnlpKfmpoLkuKTlkajlpJrvvIzku6PnoIHph4/nm7jlr7nov5jooYzvvIzmnInku4DkuYj" +
                "lubrom77lrZDmiJbogIXlpITnkIbkuI3lvZPvvIzlk6Xku6zkuZ/liKvpqoLmiJHvvIzku6XkuIrkuqTmjqXnuq/nnIvnvJjliIb" +
                "vvIzlj6/og73kvaDkuI3lgZrljZXlhYPmtYvor5XkuZ/lsLHnnIvkuI3liLAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgI" +
                "CAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgWWVvaHdhaAogICAgICA" +
                "gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg" +
                "ICAgICAgICAgICAyMDE5LzA2LzIy\n"));
    }


    /**
     * 获取商品列表数据 同步到库
     */
    @Test
    public void goodsList() {
        String token = "02f2c3bd2d61462e9347a50175349fa0b35fa3e8";
        int total = 100;
        for (int i = 1; ; i++) {
            PddGoodsListGetRequest req = getRequest(i, 100, total);
            if(req == null){
                break;
            }
            try {
                PddGoodsListGetResponse pddGoodsListGetResponse = popHttpClient.syncInvoke(req, token);
                total = pddGoodsListGetResponse.getGoodsListGetResponse().getTotalCount();
                List<PddGoodsListGetResponse.GoodsListGetResponseGoodsListItem> goodsList = pddGoodsListGetResponse.getGoodsListGetResponse().getGoodsList();
                goodsList.forEach((v) -> {
                    System.out.println(JSON.toJSONString(v));
                });
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    public PddGoodsListGetRequest getRequest(Integer page, Integer pageSize, Integer total) {
        int handle = (page - 1) * pageSize;
        if ((total - handle) < 100) {
            return null;
        }
        PddGoodsListGetRequest req = new PddGoodsListGetRequest();
        req.setPage(page);
        req.setPageSize(pageSize);
        return req;
    }

    /**
     * 测试批处理
     */
    /*@Test
    public void testBatch(){
        JobParameters jobParameters = new JobParametersBuilder().addLong("time", System.currentTimeMillis())
                .toJobParameters();
        try {
            jobLauncher.run(processJob, jobParameters);
        } catch (JobExecutionAlreadyRunningException e) {
            e.printStackTrace();
        } catch (JobRestartException e) {
            e.printStackTrace();
        } catch (JobInstanceAlreadyCompleteException e) {
            e.printStackTrace();
        } catch (JobParametersInvalidException e) {
            e.printStackTrace();
        }
    }*/

    @Test
    public void testRefresh(){
        ReguserDO reguserDO = reguserService.refreshToken(4);
    }

    @Test
    public void testReader(){
        goodsBatchBizService.reader("28150134333442");
    }
    @Test
    public void testWriter(){
        goodsBatchBizService.writer("28150134333442");
    }

}
