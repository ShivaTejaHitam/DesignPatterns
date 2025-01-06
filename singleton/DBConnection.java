class DBConnection {

  private static DBconnection connection;
  
  private DBConnection () {
  }

  public static DBConnection getInstance() {
    if(connection == null){
      connection = new DBConnection();
    }
    return connection;
  }
}
