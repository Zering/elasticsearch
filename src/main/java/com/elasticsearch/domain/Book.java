package com.elasticsearch.domain;

import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Created by zhanghaojie on 2017/10/4.
 */
public class Book {

  private String title;
  private String author;
  private int wordCount;
  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date publishDate;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public int getWordCount() {
    return wordCount;
  }

  public void setWordCount(int wordCount) {
    this.wordCount = wordCount;
  }

  public Date getPublishDate() {
    return publishDate;
  }

  public void setPublishDate(Date publishDate) {
    this.publishDate = publishDate;
  }
}
