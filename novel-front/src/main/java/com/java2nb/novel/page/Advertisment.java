package com.java2nb.novel.page;

import com.alibaba.fastjson.JSONObject;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.java2nb.novel.mapper.AdverMapper;
import com.java2nb.novel.service.AdverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping
public class Advertisment {

    @Autowired
    AdverMapper mapper;

    @Autowired
    AdverService adverService;




    /**
     * 点击一次广告后，进行记录
     */
    @GetMapping("record")
    @ResponseBody
    public JSONObject adverRecord(HttpServletRequest request) {
        adverService.clickRecode(request);

        return new JSONObject();
    }


    /**
     * js用的，在前端也需判断，是否已经点过广告了，如果点过，就不再加载了
     * @param request
     * @return
     */
    @GetMapping("isShowAdver")
    @ResponseBody
    public Map<String, Integer> isShowAdver(HttpServletRequest request) {
        Map<String, Integer> map = new HashMap<>();
        map.put("isShowAdver", adverService.isShowAdver(request));
        return map;
    }






    /**
     * 获取页面文档字串(等待异步JS执行)
     *
     * @param url 页面URL
     * @return
     * @throws Exception
     */
    public static String getHtmlPageResponse(String url) throws Exception {
        String result = "";
        final WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setThrowExceptionOnScriptError(false);//当JS执行出错的时候是否抛出异常
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);//当HTTP的状态非200时是否抛出异常
        webClient.getOptions().setActiveXNative(false);
        webClient.getOptions().setCssEnabled(false);//是否启用CSS
        webClient.getOptions().setJavaScriptEnabled(true); //很重要，启用JS
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());//很重要，设置支持AJAX
        webClient.getOptions().setTimeout(30000);//设置“浏览器”的请求超时时间
        webClient.setJavaScriptTimeout(30000);//设置JS执行的超时时间
        webClient.addRequestHeader("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Mobile Safari/537.36");
        HtmlPage page;
        try {
            page = webClient.getPage(url);
        } catch (Exception e) {
            webClient.close();
            throw e;
        }
        webClient.waitForBackgroundJavaScript(3000);//该方法阻塞线程
        result = page.asXml();
        webClient.close();
        return result;
    }


    @GetMapping("adver")
    @ResponseBody
    public Map<String, String> getAdver() throws Exception {
        String code = getHtmlPageResponse("http://127.0.0.1:8899/mobile/adver/adver.html");
        Map<String, String> result = new HashMap<>();

        int s = code.indexOf("<ins ");
        int e = code.indexOf("</ins>")+"</ins>".length();
        String content = code.substring(s, e);
        result.put("code", content);
        return result;
    }



}
