package com.elasticsearch;

import java.io.IOException;
import java.util.Date;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by zhanghaojie on 2017/9/17.
 */
@SpringBootApplication
@RestController
public class Application {

  @Autowired
  private TransportClient client;

  @GetMapping("/")
  public String index() {
    return "index";
  }

  @PostMapping("/add/book/novel")
  public ResponseEntity add(
      @RequestParam(name = "title") String title,
      @RequestParam(name = "author") String author,
      @RequestParam(name = "word_count") int wordCount,
      @RequestParam(name = "publish_date")
      @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
          Date publishDate

  ) {

    try {
      XContentBuilder builder = XContentFactory.jsonBuilder()
          .startObject()
          .field("title", title)
          .field("author", author)
          .field("word_count", wordCount)
          .field("publish_date", publishDate.getTime())
          .endObject();

      IndexResponse result = this.client.prepareIndex("book", "novel")
          .setSource(builder)
          .get();

      return new ResponseEntity(result.getId(), HttpStatus.OK);

    } catch (IOException e) {
      e.printStackTrace();
      return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
    }


  }

  @GetMapping("/get/book/novel")
  public ResponseEntity get(@RequestParam(name = "id", defaultValue = "") String id) {
    if (id.isEmpty()) {
      return new ResponseEntity(HttpStatus.NOT_FOUND);
    }

    GetResponse result = this.client.prepareGet("book", "novel", id).get();

    if (!result.isExists()) {
      return new ResponseEntity(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity(result.getSource(), HttpStatus.OK);
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

}
