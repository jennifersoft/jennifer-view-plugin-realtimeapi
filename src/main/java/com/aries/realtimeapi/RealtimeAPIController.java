package com.aries.realtimeapi;

import com.aries.extension.starter.PluginController;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Controller
public class RealtimeAPIController extends PluginController {
    private static final String JENNIFER_URL = "http://support.jennifersoft.com:27900"; // TODO: 플러그인 독립 실행시에만 URL 지정하기
//    private static final String JENNIFER_URL = null;

    @RequestMapping(value = {"/realtimeapi/domainmerged"}, method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Integer> getMainPage(@RequestParam(required=false) short[] domain_id, HttpServletRequest request) throws IOException {
        String uri = JENNIFER_URL == null ? getServerURL(request) : JENNIFER_URL;

        // 도메인 필터를 위한 Set 컬렉션 설정
        Set<Short> domainSet = new HashSet<Short>();
        if(domain_id != null) {
            for (int i = 0; i < domain_id.length; i++) {
                domainSet.add(domain_id[i]);
            }
        }

        // API 응답 데이터
        Map<String, Integer> result = new HashMap<String, Integer>();
        result.put("total", 0);
        result.put("range0", 0);
        result.put("range1", 0);
        result.put("range2", 0);
        result.put("range3", 0);

        // JENNIFER API 호출
        URL url = new URL(uri + "/api/realtime/domain");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        // 응답 문자열 JSON 객체로 파싱
        InputStream in = new BufferedInputStream(conn.getInputStream());
        String jsonStr = IOUtils.toString(in, "UTF-8");
        JSONObject jsonObj = new JSONObject(jsonStr);
        JSONArray jsonArr = jsonObj.getJSONArray("RealtimeDomainData");

        // httpClient 객체 닫기
        conn.disconnect();

        // 최종 데이터 머지하기
        for(int i = 0; i < jsonArr.length(); i++) {
            JSONObject obj = jsonArr.getJSONObject(i);
            short sid = (short) obj.getInt("domainId");

            if(domainSet.size() == 0 || domainSet.contains(sid)) {
                result.put("total", result.get("total") + obj.getInt("activeService"));
                result.put("range0", result.get("range0") + obj.getInt("activeServiceRandgeCount0"));
                result.put("range1", result.get("range1") + obj.getInt("activeServiceRandgeCount1"));
                result.put("range2", result.get("range2") + obj.getInt("activeServiceRandgeCount2"));
                result.put("range3", result.get("range3") + obj.getInt("activeServiceRandgeCount3"));
            }
        }

        return result;
    }

    private String getServerURL(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName() +
                (request.getServerPort()==80?"":":"+request.getServerPort());
    }
}
