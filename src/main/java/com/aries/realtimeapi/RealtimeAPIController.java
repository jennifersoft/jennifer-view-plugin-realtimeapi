package com.aries.realtimeapi;

import com.aries.extension.starter.PluginController;
import com.aries.extension.util.PropertyUtil;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    private static final String URL = PropertyUtil.getValue("realtimeapi", "url", "http://127.0.0.1:7900");
    private static final String TOKEN = PropertyUtil.getValue("realtimeapi", "token", "");

    @RequestMapping(value = {"/realtimeapi/domainmerged"}, method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getMainPage(@RequestParam(required=false) short[] domain_id, HttpServletRequest request) throws IOException {
        Map<String, Object> result = new HashMap<String, Object>();

        // 도메인 필터를 위한 Set 컬렉션 설정
        Set<Short> domainSet = new HashSet<Short>();
        if(domain_id != null) {
            for (int i = 0; i < domain_id.length; i++) {
                domainSet.add(domain_id[i]);
            }
        }

        // API 응답 데이터
        Map<String, Integer> activeServiceMap = new HashMap<String, Integer>();
        activeServiceMap.put("activeService", 0);
        activeServiceMap.put("range0", 0);
        activeServiceMap.put("range1", 0);
        activeServiceMap.put("range2", 0);
        activeServiceMap.put("range3", 0);

        // JENNIFER API 호출
        URL url = new URL(URL + "/api/realtime/domain?token=" + TOKEN);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        // 응답 문자열 JSON 객체로 파싱
        InputStream in = new BufferedInputStream(conn.getInputStream());
        String jsonStr = IOUtils.toString(in, "UTF-8");
        JSONObject jsonObj = new JSONObject(jsonStr);

        if(jsonObj.has("result")) {
            JSONArray jsonArr = jsonObj.getJSONArray("result");

            // httpClient 객체 닫기
            conn.disconnect();

            // 최종 데이터 머지하기
            for (int i = 0; i < jsonArr.length(); i++) {
                JSONObject obj = jsonArr.getJSONObject(i);
                short sid = (short) obj.getInt("domainId");

                if (domainSet.size() == 0 || domainSet.contains(sid)) {
                    activeServiceMap.put("activeService", activeServiceMap.get("activeService") + obj.getInt("activeService"));
                    activeServiceMap.put("range0", activeServiceMap.get("range0") + obj.getInt("activeServiceRangeCount0"));
                    activeServiceMap.put("range1", activeServiceMap.get("range1") + obj.getInt("activeServiceRangeCount1"));
                    activeServiceMap.put("range2", activeServiceMap.get("range2") + obj.getInt("activeServiceRangeCount2"));
                    activeServiceMap.put("range3", activeServiceMap.get("range3") + obj.getInt("activeServiceRangeCount3"));
                }
            }

            result.put("RealtimeDomainMergedData", activeServiceMap);
        } else {
            if(jsonObj.has("exception")) {
                return jsonObj.toMap();
            }
        }

        return result;
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Map<String, Object> handleException(Exception e, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("exception", e.toString());

        return result;
    }
}
