package com.java2nb.novel.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java2nb.novel.entity.*;
import com.java2nb.novel.mapper.CrawlSourceMapper;
import com.java2nb.novel.service.BookService;
import com.java2nb.novel.util.RegexUtils;
import com.java2nb.novel.util.RestTemplateUtils;
import io.github.xxyopen.model.resp.RestResult;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.util.ListUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
@EnableAsync
@EnableScheduling
public class BookImpl {

    @Autowired
    CrawlSourceMapper crawlSourceMapper;

    private static RestTemplate restTemplateImg = RestTemplateUtils.getInstance("utf-8");

    @Value("${pic.save.path}")
    private String imgPath;



    private String getTimeHours(int hours) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, hours);
        //        Date dateResult = dayAddAndSub(Calendar.DATE, -1); //一个月中的某天
//        Date dateResult = dayAddAndSub(Calendar.HOUR , -4); //小时
        //Date dateResult = test.dayAddAndSub(Calendar.MINUTE , -60); //分钟
        //Date dateResult = test.dayAddAndSub(Calendar.MONTH , -1); //月
        Date startDate = calendar.getTime();
        return sdf.format(startDate);
    }



    /**
     * 获取网页返回值文本
     * @param url
     * @return
     */
    private String getRes(String url, RuleBean ruleBean) {
        RestTemplate restTemplate = RestTemplateUtils.getInstance(ruleBean.getEncoding());
        HttpHeaders headers = new HttpHeaders();
        Map<String, String> header = ruleBean.getHeaders();
        for (String key : header.keySet()) {
            headers.add(key, header.get(key));
        }
        headers.add("user-agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.67 Safari/537.36");

        HttpEntity entity = new HttpEntity(null, headers);

        ResponseEntity<String> res = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        return res.getBody();
    }

    /**
     * 下载图片并返回图片名
     * @param url
     * @return
     */
    @SneakyThrows
    private String downloadPic(String url) {
        String filename = DigestUtils.md5DigestAsHex(url.getBytes()).toUpperCase();
        String imgUrl = imgPath + filename + ".jpg";
        for (int i = 0; i < 10; i++) {
            try {
                ResponseEntity<byte[]> responseEntity = restTemplateImg.exchange(url, HttpMethod.GET, null, byte[].class);
                byte[] body = responseEntity.getBody();
                FileOutputStream fileOutputStream = new FileOutputStream(new File(imgUrl));
                fileOutputStream.write(body);
                fileOutputStream.close();
                return "/"+filename+".jpg";
            } catch (Exception e) {

            }
        }
        return "";


    }


    /**
     * 插入新书
     * @return
     */
    private Integer insertNewBook(Book book) {
        Book qBook = crawlSourceMapper.selectByNameAuthor(book.getBookName().trim(), book.getAuthorName().trim());
        if (qBook != null) {
            return 0;
        }
        // 补全书籍信息
        BookCategory category = crawlSourceMapper.seleCatById(book.getCatId());
        book.setCatName(category.getName());
        book.setWorkDirection(Byte.parseByte("0"));
        book.setScore(0f);
        // 下载图片并获取文件名
        if (book.getPicUrl().equals("")) {
            book.setPicUrl("/images/default.gif");
        } else {
            String imgUrl = downloadPic(book.getPicUrl());
            if (imgUrl.equals("")) {
                book.setPicUrl("/images/default.gif");
            } else {
                book.setPicUrl("/mainPic"+imgUrl);
            }

        }

        return crawlSourceMapper.insertBook(book);
    }

    /**
     * 根据页面信息，以及起始位置和结束位置，直接截取出小说内容
     * @param res
     * @return
     */
    private String getContent(String res, String start, String end) {
        Integer beginIndex = res.indexOf(start) + start.length();
        Integer endIndex = res.indexOf(end);
        String content = res.substring(beginIndex, endIndex);

        return content;
    }

    /**
     * 插入目录、内容，事务控制，防止数据不统一
     * @param ruleBean
     * @param bookId
     * @param res
     * @param chapterList
     * @param contentUrlList
     * @param j
     */
    @Transactional
    void insertChapterContent(RuleBean ruleBean, Long bookId, String res, List<String> chapterList, List<String> contentUrlList, int j) {
        // 插入章节信息、内容文本
        String contentUrl = ruleBean.getChapterUrlFore().equals("") ? contentUrlList.get(j) : ruleBean.getChapterUrlFore() + contentUrlList.get(j);
        if (ruleBean.getChapterUrlForeReg() != null && !ruleBean.getChapterUrlForeReg().equals("")) {
            contentUrl = RegexUtils.parseExpressContent(ruleBean.getChapterUrlForeReg(), res) + contentUrl;
        }

        BookIndex bookIndex = new BookIndex();
        bookIndex.setBookId(bookId);
        bookIndex.setIndexNum(j);
        bookIndex.setIndexName(chapterList.get(j).trim());

        // 插入文本
        try {
            res = getRes(contentUrl, ruleBean);
        } catch (Exception e) {
            return;
        }

        // 判断获取内容，是用正则，还是直接首位截取
        String content = "";
        try {
            if (!ruleBean.getContentRegex().equals("")) {
                content = RegexUtils.parseExpressContent(ruleBean.getContentRegex(), res);
            } else {
                content = getContent(res, ruleBean.getContentStartExp(), ruleBean.getContentEndExp());
            }
        }catch (Exception e) {
            return;
        }


        content = content.replace(ruleBean.getContentclean(), "");
        bookIndex.setWordCount(content.replace("&nbsp;", "").length());
        crawlSourceMapper.insertIndex(bookIndex);

        BookContent bookContent = new BookContent();
        Long indexId = bookIndex.getId();
        bookContent.setIndexId(indexId);
        bookContent.setContent(content);
        String contentNum = "book_content"+String.valueOf(indexId % 10);
        crawlSourceMapper.insertContent(bookContent, contentNum);

        // 更新书的最新章节信息
        crawlSourceMapper.updateBook(1L, indexId, bookIndex.getIndexName(), new Date(), bookId);
    }


    void updateChapterContent(BookIndex bookIndex, String tableIndex, RuleBean ruleBean, String res, List<String> chapterList, List<String> contentUrlList, int j) {
        // 先获取内容
        String contentUrl = ruleBean.getChapterUrlFore().equals("") ? contentUrlList.get(j) : ruleBean.getChapterUrlFore() + contentUrlList.get(j);
        if (ruleBean.getChapterUrlForeReg() != null && !ruleBean.getChapterUrlForeReg().equals("")) {
            contentUrl = RegexUtils.parseExpressContent(ruleBean.getChapterUrlForeReg(), res) + contentUrl;
        }

        res = getRes(contentUrl, ruleBean);
        // 判断获取内容，是用正则，还是直接首位截取
        String content = "";
        if (!ruleBean.getContentRegex().equals("")) {
            content = RegexUtils.parseExpressContent(ruleBean.getContentRegex(), res);
        } else {
            content = getContent(res, ruleBean.getContentStartExp(), ruleBean.getContentEndExp());
        }

        content = content.replace(ruleBean.getContentclean(), "");

        // 目录更新
        bookIndex.setIndexNum(j);
        bookIndex.setIndexName(chapterList.get(j).trim());
        bookIndex.setWordCount(content.replace("&nbsp;", "").length());
        crawlSourceMapper.updateIndex(bookIndex);

        // 内容更新
        crawlSourceMapper.updateContent(content, bookIndex.getId(), tableIndex);

    }


    private void downloadBook(String url, RuleBean ruleBean, String localCatId, Integer sourceId) {
        String res = getRes(url, ruleBean);
        // 获取书名、作者、图片、描述
        String bookName = RegexUtils.parseExpressContent(ruleBean.getBookNameExp(), res);
        if (bookName.endsWith("最新章节")) {
            bookName = bookName.substring(0, bookName.length() - 4);
        }
        String author = RegexUtils.parseExpressContent(ruleBean.getAuthorExp(), res);
        String imgTemp = "";
        String img = "";
        if (!ruleBean.getImgExp().equals("")) {
            imgTemp = RegexUtils.parseExpressContent(ruleBean.getImgExp(), res);
            img = ruleBean.getImgForeUrl().equals("")?imgTemp:ruleBean.getImgForeUrl()+imgTemp;
        }

        String desc = "";
        if (!ruleBean.getDescExp().equals("")) {
            desc = RegexUtils.parseExpressContent(ruleBean.getDescExp(), res);
        }

        String status = "";
        if (res.contains(ruleBean.getStatusExp())) {
            status = "1";
        } else {
            status = "0";
        }

        Book book = new Book();
        book.setBookName(bookName);
        book.setAuthorName(author);
        book.setPicUrl(img);
        book.setBookDesc(desc);
        book.setBookStatus(Byte.parseByte(status));

        book.setCatId(Integer.parseInt(localCatId));
        book.setSpiderDetail(url);
        book.setCrawlSourceId(sourceId);

        // 插入新书，如果已经存在，则返回0，否则继续获取章节内容

        if (insertNewBook(book) == 1) {
            Long bookId = book.getId();
            // 插入新书成功， 开始获取章节信息
            List<String> chapterListTemp = RegexUtils.parseExpressList(ruleBean.getChapterListExp(), res);
            List<String> contentUrlListTemp = RegexUtils.parseExpressList(ruleBean.getChapterUrlExp(), res);
            // 判断前几章，是否有重复
            List<String> chapterList = new ArrayList<>();
            List<String> contentUrlList = new ArrayList<>();
            for (int i = 0; i < chapterListTemp.size(); i++) {
                boolean flag = true;
                for (int j = 0; j < chapterListTemp.size(); j++) {
                    if (i < j) {
                        if (chapterListTemp.get(i).equals(chapterListTemp.get(j))) {
                            flag = false;
                        }
                    }
                }
                if (flag) {
                    chapterList.add(chapterListTemp.get(i));
                    contentUrlList.add(contentUrlListTemp.get(i));
                }
            }




            for (int j = 0; j < chapterList.size(); j++) {
                // 插入所有的目录和内容
                insertChapterContent(ruleBean, bookId, res, chapterList, contentUrlList, j);
            }

            // 章节全部插入成功之后，计算一下总字数，回写book表。获取最后的章节，作为最近更新章节
            Long totalWords = crawlSourceMapper.selectCalcWords(bookId);
            BookIndex lastIndex = crawlSourceMapper.selectOneIndex(bookId);
            crawlSourceMapper.updateBook(totalWords, lastIndex.getId(), lastIndex.getIndexName(), lastIndex.getUpdateTime(), bookId);
        }
    }


    /**
     * 获取书列表页，书id，传给详情页
     * @param ruleBean
     * @param targetCate
     */
    @Async
    public void getBookList(RuleBean ruleBean, String targetCate, String localCatId, Integer sourceId) {
        Integer page = 1;
        // 书客吧从第二页开始
        if (ruleBean.getBookListUrl().contains("www.shukeba.com")) {
            page = 2;
        } else {
            page = 1;
        }

        Integer total = page;
        String[] targetCateList = targetCate.split(",");
        String url = "";
        if (targetCateList.length == 1) {
            url = ruleBean.getBookListUrl().replace("{catId}", targetCateList[0]).replace("{page}", String.valueOf(page));
        } else {
            url = ruleBean.getBookListUrl().replace("{catId}", targetCateList[0]).replace("{page}", String.valueOf(page)).replace("{catMap}", targetCateList[1]);
        }

        String res = getRes(url, ruleBean);
        // 获取当前品类总页数
        if (ruleBean.getTotalPageExp().equals("1")) {
            total = 1;
        } else {
            total = Integer.valueOf(RegexUtils.parseExpressContent(ruleBean.getTotalPageExp(), res));
        }

        for (page = page; page <= total + 1; page++) {
            // 获取每一页所有书籍
            if (targetCateList.length == 1) {
                url = ruleBean.getBookListUrl().replace("{catId}", targetCateList[0]).replace("{page}", String.valueOf(page));
            } else {
                url = ruleBean.getBookListUrl().replace("{catId}", targetCateList[0]).replace("{page}", String.valueOf(page)).replace("{catMap}", targetCateList[1]);
            }
            res = getRes(url, ruleBean);
            // 获取详情列表
            List<String> detailList = RegexUtils.parseExpressList(ruleBean.getDetailExp(), res);

            // 当前页，逐本书爬取
            for (String u : detailList) {
                u = ruleBean.getForeUrl().equals("")?u:ruleBean.getForeUrl() + u;
                downloadBook(u, ruleBean, localCatId, sourceId);
            }
        }

    }



    @Async
    @SneakyThrows
    public RestResult newBookSpider(String id) {
        Spider spider = crawlSourceMapper.selectById(Long.valueOf(id));
        RuleBean ruleBean = new ObjectMapper().readValue(spider.getRule(), RuleBean.class);
        // 逐个品类抓取
        Map<String, String> cate = ruleBean.getCatId();
        // 打乱分类顺序
        List<String> newSort = new ArrayList<>();
        newSort.addAll(cate.keySet());
        Collections.shuffle(newSort);
        // 开始按照分类爬取
        for (String localCate : newSort) {
            String targetCate = cate.get(localCate);
            if (targetCate.equals("")) {
                continue;
            }
            getBookList(ruleBean, targetCate, localCate, Integer.parseInt(id));
        }


        return RestResult.ok();
    }


    /**
     * 旧书更新,根据book表的spider_detail,获取章节信息和库中的对比
     * 若章节数为0，则插入全新章节；若章数增多，则把现有的更新，多出的插入；若章数减少，则把现有的全部删除，按照当前的插入
     * @param type  任务类型  0 断章补全   1  检查更新
     * @return
     */
    @SneakyThrows
    public void updateBook(int type) {
        List<Book> books = new ArrayList<>();
        // 随机选择100本书
        if (type == 0) {
            books = crawlSourceMapper.selectBookIncomplete(getTimeHours(-3));
        } else if (type == 1){
            books = crawlSourceMapper.selectBookupdate();
        }

        for (Book book : books) {
            // 获取该书的数据源信息
            RuleBean ruleBean = new ObjectMapper().readValue(crawlSourceMapper.selectSourceByBook(book.getCrawlSourceId()), RuleBean.class);
            // 获取当前章节数
            String res = getRes(book.getSpiderDetail(), ruleBean);
            List<String> resultTemp = RegexUtils.parseExpressList(ruleBean.getChapterListExp(), res);
            List<String> result = new ArrayList<>();
            for (int i = 0; i < resultTemp.size(); i++) {
                boolean flag = true;
                for (int j = 0; j < resultTemp.size(); j++) {
                    if (i < j) {
                        if (resultTemp.get(i).equals(resultTemp.get(j))) {
                            flag = false;
                        }
                    }
                }
                if (flag) {
                    result.add(resultTemp.get(i));
                }
            }

            // 获取数据库中该书的章节数
            Integer chaptersCount = crawlSourceMapper.selectChapterCounts(book.getId());
            if (chaptersCount == 0) {
                Long bookId = book.getId();
                // 开始获取章节信息
                List<String> chapterListTemp = RegexUtils.parseExpressList(ruleBean.getChapterListExp(), res);
                List<String> contentUrlListTemp = RegexUtils.parseExpressList(ruleBean.getChapterUrlExp(), res);
                // 判断前几章，是否有重复
                List<String> chapterList = new ArrayList<>();
                List<String> contentUrlList = new ArrayList<>();
                for (int i = 0; i < chapterListTemp.size(); i++) {
                    boolean flag = true;
                    for (int j = 0; j < chapterListTemp.size(); j++) {
                        if (i < j) {
                            if (chapterListTemp.get(i).equals(chapterListTemp.get(j))) {
                                flag = false;
                            }
                        }
                    }
                    if (flag) {
                        chapterList.add(chapterListTemp.get(i));
                        contentUrlList.add(contentUrlListTemp.get(i));
                    }
                }


                for (int j = 0; j < chapterList.size(); j++) {
                    // 插入所有的目录和内容
                    insertChapterContent(ruleBean, bookId, res, chapterList, contentUrlList, j);
                }

                // 章节全部插入成功之后，计算一下总字数，回写book表。获取最后的章节，作为最近更新章节
                Long totalWords = crawlSourceMapper.selectCalcWords(bookId);
                BookIndex lastIndex = crawlSourceMapper.selectOneIndex(bookId);
                crawlSourceMapper.updateBook(totalWords, lastIndex.getId(), lastIndex.getIndexName(), lastIndex.getUpdateTime(), bookId);

            } else if (chaptersCount < result.size()) {

                Long bookId = book.getId();
                // 开始获取章节信息
                List<String> chapterListTemp = RegexUtils.parseExpressList(ruleBean.getChapterListExp(), res);
                List<String> contentUrlListTemp = RegexUtils.parseExpressList(ruleBean.getChapterUrlExp(), res);
                // 判断前几章，是否有重复
                List<String> chapterList = new ArrayList<>();
                List<String> contentUrlList = new ArrayList<>();
                for (int i = 0; i < chapterListTemp.size(); i++) {
                    boolean flag = true;
                    for (int j = 0; j < chapterListTemp.size(); j++) {
                        if (i < j) {
                            if (chapterListTemp.get(i).equals(chapterListTemp.get(j))) {
                                flag = false;
                            }
                        }
                    }
                    if (flag) {
                        chapterList.add(chapterListTemp.get(i));
                        contentUrlList.add(contentUrlListTemp.get(i));
                    }
                }

                for (int j = 0; j < chapterList.size(); j++) {

                    // 即将更新的章节和索引是否存在
                    BookIndex bookIndexName = crawlSourceMapper.selectChapterByBookIndexName(bookId, j, chapterList.get(j));
                    if (bookIndexName == null) {
                        // 不存在，判断索引是否存在
                        BookIndex bookIndex = crawlSourceMapper.selectChapterByBookIndex(bookId, j);
                        if (bookIndex == null) {
                            // 索引也不存在，新增
                            insertChapterContent(ruleBean, bookId, res, chapterList, contentUrlList, j);
                        } else {
                            // 索引存在，按照索引更新原数据
                            String tableIndex = "book_content" + bookIndex.getId() % 10;
                            updateChapterContent(bookIndex, tableIndex, ruleBean, res,  chapterList,  contentUrlList, j);
                        }

                    } else {
                        // 存在即跳过
                        continue;
                    }

                }

                // 章节全部插入成功之后，计算一下总字数，回写book表。获取最后的章节，作为最近更新章节
                Long totalWords = crawlSourceMapper.selectCalcWords(bookId);
                BookIndex lastIndex = crawlSourceMapper.selectOneIndex(bookId);
                crawlSourceMapper.updateBook(totalWords, lastIndex.getId(), lastIndex.getIndexName(), lastIndex.getUpdateTime(), bookId);

            } else if (chaptersCount > result.size()) {

            } else {

            }

        }

    }


    /**
     * 爬取指定源的书籍信息
     */
    @SneakyThrows
    private void designatedBook() {
        List<DesignatedBook> designatedBook = crawlSourceMapper.selectDesignatedList();
        for (DesignatedBook book : designatedBook) {
            RuleBean ruleBean = new ObjectMapper().readValue(crawlSourceMapper.selectSourceByBook(book.getSpiderId()), RuleBean.class);
            crawlSourceMapper.updateDesignatedBookBegin(book.getId(), 1);
            downloadBook(book.getSourceUrl(), ruleBean, book.getCatId().toString(), book.getSpiderId());
            crawlSourceMapper.updateDesignatedBookBegin(book.getId(), 2);
        }

    }


    /**
     * *******************************************************************************************************************
     */



    /**
     * 新书任务
     */
//    @Scheduled(cron = "* */5 * * * ?")
    public void getNewBookTask() {
        // 获取所有线程
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        int total = Thread.activeCount();
        Thread[] threads = new Thread[total];
        threadGroup.enumerate(threads);

        Set<String> tnames = new HashSet<>();
        for (Thread t : threads) {
            String tname = t.getName();
            tnames.add(tname);
        }

        // 遍历所有爬虫源
        List<Spider> spiders = crawlSourceMapper.selectSource();
        for (Spider spider : spiders) {
            // 爬虫线程名称
            String spiderName = "newBook" + spider.getId();
            if (!tnames.contains(spiderName)) {
                // 如果线程不存在，则创建线程  newBook+源id
                Thread t = new Thread(()->BookImpl.this.newBookSpider(spider.getId().toString()));
                t.setName(spiderName);
                t.start();

            }
        }
    }

    /**
     * 完善新书断章任务
     */
    @Scheduled(cron = "* */1 * * * ?")
    public void updateBookTask0() {
        // 获取所有线程
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        int total = Thread.activeCount();
        Thread[] threads = new Thread[total];
        threadGroup.enumerate(threads);

        Set<String> tnames = new HashSet<>();
        for (Thread t : threads) {
            String tname = t.getName();
            tnames.add(tname);
        }

        // 检查是否已经存在更新任务
        String updateTaskName = "updateBookTask0";
        if (!tnames.contains(updateTaskName)) {
            Thread t = new Thread(() -> BookImpl.this.updateBook(0));
            t.setName(updateTaskName);
            t.start();
        }
    }


    /**
     * 全部书籍检查更新任务
     */
    @Scheduled(cron = "* */30 * * * ?")
    public void updateBookTask1() {
        // 获取所有线程
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        int total = Thread.activeCount();
        Thread[] threads = new Thread[total];
        threadGroup.enumerate(threads);

        Set<String> tnames = new HashSet<>();
        for (Thread t : threads) {
            String tname = t.getName();
            tnames.add(tname);
        }

        // 检查是否已经存在更新任务
        String updateTaskName = "updateBookTask1";
        if (!tnames.contains(updateTaskName)) {
            Thread t = new Thread(() -> BookImpl.this.updateBook(1));
            t.setName(updateTaskName);
            t.start();
        }
    }






    /**
     * 爬取指定书籍任务
     */
//    @Scheduled(cron = "* */1 * * * ?")
    public void designatedBookTask() {
        // 获取所有线程
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        int total = Thread.activeCount();
        Thread[] threads = new Thread[total];
        threadGroup.enumerate(threads);

        Set<String> tnames = new HashSet<>();
        for (Thread t : threads) {
            String tname = t.getName();
            tnames.add(tname);
        }

        // 检查是否已经存在更新任务
        String updateTaskName = "designatedBookTask";
        if (!tnames.contains(updateTaskName)) {
            Thread t = new Thread(() -> BookImpl.this.designatedBook());
            t.setName(updateTaskName);
            t.start();
        }

    }








    /**
     * 爬取指定源的书籍信息
     */
    @SneakyThrows
    private void multiDesignatedBook(DesignatedBook book) {
        RuleBean ruleBean = new ObjectMapper().readValue(crawlSourceMapper.selectSourceByBook(book.getSpiderId()), RuleBean.class);
        crawlSourceMapper.updateDesignatedBookBegin(book.getId(), 1);
        downloadBook(book.getSourceUrl(), ruleBean, book.getCatId().toString(), book.getSpiderId());
        crawlSourceMapper.updateDesignatedBookBegin(book.getId(), 2);

    }


    /**
     * 多线程爬取指定书籍   12个线程
     */
    @SneakyThrows
//    @Scheduled(cron = "*/30 * * * * ?")
    public void multiDesignatedBookTask() {
        // 获取所有线程
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        int total = Thread.activeCount();
        Thread[] threads = new Thread[total];
        threadGroup.enumerate(threads);

        Set<String> tnames = new HashSet<>();
        for (Thread t : threads) {
            String tname = t.getName();
            tnames.add(tname);
        }


        for (int i = 0; i < 12; i++) {
            String updateTaskName = "multiDesignatedBookTask" + i;
            if (!tnames.contains(updateTaskName)) {
                List<DesignatedBook> designatedBook = crawlSourceMapper.selectDesignatedList();
                DesignatedBook book = designatedBook.get(i);
                Thread t = new Thread(() -> BookImpl.this.multiDesignatedBook(book));
                t.setName(updateTaskName);
                t.start();
                Thread.sleep(2000);
            }

        }
    }

}
