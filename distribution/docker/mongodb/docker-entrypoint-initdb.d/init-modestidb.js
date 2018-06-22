db.createUser(
  {
    user: "modestiUser",
    pwd: "password",
    roles: [ { role: "readWrite", db: "modestidb" } ]
  }
)
