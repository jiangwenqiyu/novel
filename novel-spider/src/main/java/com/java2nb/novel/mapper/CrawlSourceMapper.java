package com.java2nb.novel.mapper;

import com.java2nb.novel.entity.*;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface CrawlSourceMapper {

    @Select("select * from spider where id=#{id}")
    Spider selectById(Long id);


    @Select("select * from book where book_name=#{bookName} and author_name=#{authorName}")
    Book selectByNameAuthor(String bookName, String authorName);

    @Select("select * from book_category where id=#{catId}")
    BookCategory seleCatById(Integer catId);

    @Insert({"insert into book(work_direction,cat_id,cat_name,pic_url,book_name,author_name,book_desc,score,book_status,word_count,spider_detail,crawl_source_id)",
    "values(#{workDirection},#{catId},#{catName},#{picUrl},#{bookName},#{authorName},#{bookDesc},#{score},#{bookStatus},#{wordCount},#{spiderDetail},#{crawlSourceId})"})
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Integer insertBook(Book book);

    @Insert("insert into book_index(book_id, index_num, index_name, word_count) values(#{bookId}, #{indexNum}, #{indexName}, #{wordCount})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertIndex(BookIndex bookIndex);

    @Insert("insert into ${contentNum}(index_id, content) values(#{bookContent.indexId}, #{bookContent.content})")
    void insertContent(@Param("bookContent") BookContent bookContent, @Param("contentNum") String contentNum);

    @Select("select sum(s.word_count) from book_index s where s.book_id = #{bookId}")
    Long selectCalcWords(Long bookId);

    @Select("select * from book_index where book_id=#{bookId} order by id desc limit 1")
    BookIndex selectOneIndex(Long bookId);

    @Update("update book set word_count=#{totalWords}, last_index_id=#{indexId}, last_index_name=#{indexName}, last_index_update_time=#{updateTime} where id=#{bookId}")
    void updateBook(Long totalWords, Long indexId, String indexName, Date updateTime, Long bookId);


//    @Select("select * from book where book_name='朱平安'")
    @Select("select * from book where create_time <= #{now} and word_count=1")
    List<Book> selectBookIncomplete(String now);


    @Select("select id, book_name, author_name, crawl_source_id, spider_detail from book")
    List<Book> selectBookupdate();

    @Select("select rule from spider where id=#{crawlSourceId}")
    String selectSourceByBook(Integer crawlSourceId);

    @Select("select count(0) from book_index where book_id=#{id}")
    Integer selectChapterCounts(Long id);

    @Select("select * from book_index where book_id=#{bookId} and index_num=#{j} and index_name=#{index_name}")
    BookIndex selectChapterByBookIndexName(Long bookId, int j, String index_name);

    @Select("select * from book_index where book_id=#{bookId} and index_num=#{j}")
    BookIndex selectChapterByBookIndex(Long bookId, int j);

    @Update("update book_index set index_num=#{indexNum}, index_name=#{indexName}, word_count=#{wordCount} where id=#{id}")
    void updateIndex(BookIndex bookIndex);

    @Update("update ${tableIndex} set content=#{content} where index_id=#{indexId}")
    void updateContent(@Param("content") String content, @Param("indexId") Long indexId, @Param("tableIndex") String tableIndex);

    @Select("select * from spider where status=0")
    List<Spider> selectSource();

    @Select("select * from designatedbook where status=0")
    List<DesignatedBook> selectDesignatedList();

    @Update("update designatedbook set status=#{status} where id=#{id}")
    void updateDesignatedBookBegin(@Param("id") Long id, @Param("status") Integer status);
}
