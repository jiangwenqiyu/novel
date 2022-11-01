package com.java2nb.novel.mapper;

import com.java2nb.novel.entity.Book;
import com.java2nb.novel.entity.BookContent;
import com.java2nb.novel.vo.BookIndexVo;
import org.apache.ibatis.annotations.*;

import javax.annotation.Generated;
import java.util.Date;

@Mapper
public interface SpiderMapper {

    @Select("select * from book where book_name = #{name} and author_name = #{author}")
    Book selectBookByNameAuthor(String name, String author);


    @Insert({"insert into book(work_direction,cat_id,cat_name,pic_url,book_name,author_name,book_desc,score,book_status,word_count,is_vip,status, crawl_book_id)",
            "values(#{workDirection},#{catId},#{catName},#{picUrl},#{bookName},#{authorName},#{bookDesc},#{score},#{bookStatus},#{wordCount},#{isVip},#{status}, #{crawlBookId})"})
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Long insertBook(Book book);


    @Insert({"insert into book_index(book_id,index_num,index_name,is_vip,book_price, word_count)",
            "values(#{bookId},#{indexNum},#{indexName},#{isVip},#{bookPrice},#{wordCount})"})
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Long insertIndex(BookIndexVo bookIndexVo);

    @Insert({"insert into ${bookContent}(index_id,content)",
            "values(#{content.indexId},#{content.content})"})
    void insertContent(@Param("content") BookContent content, @Param("bookContent") String bookContent);

    @Update("update book set last_index_id=#{indexId},last_index_name=#{indexName}, last_index_update_time=#{date} where id=#{bookId}")
    void updateBookLastedIndex(Long indexId, String indexName, Date date, Long bookId);

    @Select("select count(0) from book_index where book_id=#{id}")
    Integer selectIndexCountByBookId(Long id);
}
