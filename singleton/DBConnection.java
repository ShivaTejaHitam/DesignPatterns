class DBConnection {

  static DBconnection connection;
  
  private DBConnection () {
  }

  static DBConnection getInstance() {
    if(connection == null){
      connection = new DBConnection();
    }
    return connection;
  }
}
