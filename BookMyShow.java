class Theatre {
  private String theatreId;
  private String cityId;
  private String theatreName;
}

class Ticket{
  private String ticketId;
  private String showId;
  private String price;
}

class Show {
  private String showId;
  private Instant startTime;
  private Instant endTime;
  private String theatreId;
}

class Movie {
  private String movieId;
  private String name;
  private String language;
}

class City {
  private String id;
  private String name;
}

class MovieTheatre {
  private String movieId;
  private String theatreId;
}
