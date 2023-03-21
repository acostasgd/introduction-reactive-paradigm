package com.griddynamics.reactive.document;

import java.util.Objects;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(value = "users")
public class User {

  @Id private String id;
  private String name;
  private String phone;

  @PersistenceCreator
  public User(String id, String name, String phone) {
    this.id = id;
    this.name = name;
    this.phone = phone;
  }

  public User(String name, String phone) {
    this.name = name;
    this.phone = phone;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    User user = (User) o;
    return Objects.equals(id, user.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
