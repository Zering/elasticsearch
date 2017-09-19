package com.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * elasticsearch java api
 *
 * Created by zhanghaojie on 2017/9/17.
 */
@SpringBootApplication
@RestController
public class Application {

  private final TransportClient client;

  @Autowired
  public Application(TransportClient client) {
    this.client = client;
  }

  @GetMapping("/")
  public String index() {
    return "index";
  }

  @PostMapping("/add/book/novel")
  public ResponseEntity<String> add(
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

      return new ResponseEntity<>(result.getId(), HttpStatus.OK);

    } catch (IOException e) {
      e.printStackTrace();
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }


  }

  @GetMapping("/get/book/novel")
  public ResponseEntity<Map<String, Object>> get(@RequestParam(name = "id", defaultValue = "") String id) {
    if (id.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    GetResponse result = this.client.prepareGet("book", "novel", id).get();

    if (!result.isExists()) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(result.getSource(), HttpStatus.OK);
  }

  @PutMapping("/update/book/novel")
  public ResponseEntity<String> update(
      @RequestParam(name = "id") String id,
      @RequestParam(name = "title", required = false) String title,
      @RequestParam(name = "author", required = false) String author){
    UpdateRequest request = new UpdateRequest("book", "novel", id);
    try {
      XContentBuilder builder = XContentFactory.jsonBuilder();

      builder.startObject();
      if (title != null) {
        builder.field("title", title);
      }
      if (author != null){
        builder.field("author", author);
      }
      builder.endObject();

      request.doc(builder);

    } catch (IOException e) {
      e.printStackTrace();
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    try {
      UpdateResponse result = this.client.update(request).get();
      return new ResponseEntity<>(result.getResult().toString(), HttpStatus.OK);
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

  }

  @DeleteMapping("/delete/book/novel")
  public ResponseEntity<String> delete(
      @RequestParam(name = "id") String id
  ) {
    DeleteResponse result = this.client.prepareDelete("book", "novel", id).get();
    return new ResponseEntity<>(result.getResult().toString(), HttpStatus.OK);
  }

  @PostMapping("/query/book/novel")
  public ResponseEntity<List<Map<String, Object>>> query(
      @RequestParam(name = "title", required = false) String title,
      @RequestParam(name = "author", required = false) String author,
      @RequestParam(name = "gt_word_count", defaultValue = "0") int gtWordCount,
      @RequestParam(name = "lt_word_count", required = false) int ltWordCount
  ){
    BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

    if (title != null) {
      boolQueryBuilder.must(QueryBuilders.matchQuery("title", title));
    }

    if (author != null) {
      boolQueryBuilder.must(QueryBuilders.matchQuery("author", author));
    }

    RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("word_count");

    rangeQuery.from(gtWordCount);
    if (ltWordCount > 0) {
      rangeQuery.to(ltWordCount);
    }
    boolQueryBuilder.filter(rangeQuery);

    SearchRequestBuilder builder = this.client.prepareSearch("book")
        .setTypes("novel")
        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
        .setQuery(boolQueryBuilder)
        .setFrom(0)
        .setSize(10);

    System.out.println(builder);

    SearchResponse response = builder.get();
    List<Map<String, Object>> result = new ArrayList<>();
    for (SearchHit hitFields : response.getHits()){
      result.add(hitFields.getSource());
    }

    return new ResponseEntity<>(result, HttpStatus.OK);

  }


  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

}
