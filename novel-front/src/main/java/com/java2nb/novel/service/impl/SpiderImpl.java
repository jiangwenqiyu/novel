package com.java2nb.novel.service.impl;

import com.java2nb.novel.entity.Book;
import com.java2nb.novel.entity.BookContent;
import com.java2nb.novel.mapper.SpiderMapper;
import com.java2nb.novel.service.SpiderService;
import com.java2nb.novel.service.ex.NameAuthorDuplicated;
import com.java2nb.novel.vo.BookIndexVo;
import com.java2nb.novel.vo.SpiderVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class SpiderImpl implements SpiderService {
    @Autowired
    SpiderMapper spiderMapper;

    @Value("${pic.save.path}")
    private String picPath;

    private String downloadPic(String url, String bookname, String author) throws IOException {
        // 根据书名和作者生成md5,图片名称
        String picname = DigestUtils.md5DigestAsHex((bookname + author).getBytes()).toUpperCase();

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<byte[]> responseEntity = restTemplate.exchange(url, HttpMethod.GET, null, byte[].class);
        byte[] body = responseEntity.getBody();
        FileOutputStream fileOutputStream = new FileOutputStream(new File(picPath+ picname + ".jpg") );
        fileOutputStream.write(body);
        fileOutputStream.close();
        return "/mainPic/"+picname+".jpg";
    }





    @Override
    @Transactional
    public void insert(SpiderVo spiderVo) throws Exception {
        // 校验书和作者是否已经存在
        Book b = spiderMapper.selectBookByNameAuthor(spiderVo.getBookName().trim(), spiderVo.getAuthorName().trim());
        if (b != null) {
            // 如果已经存在，判断,是否同一个数据源
            if (b.getCrawlBookId().equals(spiderVo.getCrawlBookId())) {
                Long bookId = b.getId();
                Integer indexCounts = spiderMapper.selectIndexCountByBookId(bookId);
                if (indexCounts < spiderVo.getBookIndexList().size()) {
                    // 不相同的时候更新，否则不用管
                    insertIndexContent(bookId, spiderVo, 2, indexCounts);
                }
            }


        } else {
            // 下载图片且更新图片地址
            String localUrl = downloadPic(spiderVo.getPicUrl(), spiderVo.getBookName(), spiderVo.getAuthorName());
            spiderVo.setPicUrl(localUrl);

            // 补全book其余属性
            Book book = new Book();
            BeanUtils.copyProperties(spiderVo, book);
            book.setBookName(book.getBookName().trim());
            book.setAuthorName(book.getAuthorName().trim());
            book.setWorkDirection(Byte.parseByte("0"));
            book.setScore(0f);
            book.setBookStatus(Byte.parseByte("0"));
            book.setIsVip(Byte.parseByte("0"));
            book.setStatus(Byte.parseByte("1"));

            // 插入book，获取bookId
            spiderMapper.insertBook(book);
            Long bookId = book.getId();
            insertIndexContent(bookId, spiderVo, 1, 0);

        }


    }

    /**
     * 插入目录和内容
     * @param bookId
     * @param spiderVo
     * @param type 1 新增   2 更新
     */
    private void insertIndexContent(Long bookId, SpiderVo spiderVo, Integer type, Integer historyNum) {
        List<BookIndexVo> bookIndexVoList = new ArrayList<>();
        int index_num = historyNum;

        if (type == 1) {
            bookIndexVoList = spiderVo.getBookIndexList();
        } else {
            bookIndexVoList = spiderVo.getBookIndexList().subList(historyNum, spiderVo.getBookIndexList().size());
        }


        // 添加bookid, 补全信息,插入目录
        for (BookIndexVo bookIndexVo : bookIndexVoList) {
            bookIndexVo.setBookId(bookId);
            bookIndexVo.setIndexNum(index_num);
            bookIndexVo.setIsVip(Byte.parseByte("0"));
            bookIndexVo.setBookPrice(0);
            // 插入目录，获取id
            spiderMapper.insertIndex(bookIndexVo);
            Long indexId = bookIndexVo.getId();
            index_num++;

            // 把indexId写入content; 根据index,获取到book_content索引
            Long i = indexId % 10;
            String bookContent = "book_content" + i;

            BookContent content = bookIndexVo.getBookContent();
            content.setIndexId(indexId);
            spiderMapper.insertContent(content, bookContent);

            // 判断是否是最后的，如果是，更新book表最新章节
            if (index_num == spiderVo.getBookIndexList().size()) {
                spiderMapper.updateBookLastedIndex(indexId, bookIndexVo.getIndexName(), new Date(), bookId);
            }
        }
    }
}
