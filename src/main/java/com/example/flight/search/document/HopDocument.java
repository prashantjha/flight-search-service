package com.example.flight.search.document;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

public class HopDocument {

  @Field(type = FieldType.Integer)
  private Integer hopOrder;

  @Field(type = FieldType.Keyword)
  private String source;

  @Field(type = FieldType.Keyword)
  private String destination;

  @Field(type = FieldType.Long)
  private Long hopId;

  // Constructors
  public HopDocument() {}

  public HopDocument(Integer hopOrder, String source, String destination, Long hopId) {
    this.hopOrder = hopOrder;
    this.source = source;
    this.destination = destination;
    this.hopId = hopId;
  }

  // Getters and Setters
  public Integer getHopOrder() {
    return hopOrder;
  }

  public void setHopOrder(Integer hopOrder) {
    this.hopOrder = hopOrder;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getDestination() {
    return destination;
  }

  public void setDestination(String destination) {
    this.destination = destination;
  }

  public Long getHopId() {
    return hopId;
  }

  public void setHopId(Long hopId) {
    this.hopId = hopId;
  }
}
