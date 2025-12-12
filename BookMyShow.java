class City {
  private String id;
  private String name;
}

class Theatre {
  private String theatreId;
  private String cityId;
  private String theatreName;
  private int totalSeats;
}

class Movie {
  private String movieId;
  private String name;
  private String language;
  private String genre;
  private int durationInMinutes;
}

class Booking{
  private String bookingId;
  private List<String> seatIds;
  private String showId;
  private String price;
  private BookingStatus status;
}

class Show {
  private String showId;
  private Instant startTime;
  private Instant endTime;
  private String theatreId;
}

class Seat {
  private String seatId;
  private String seatNo;
  private SeatType seatType;
  private String theatreId;
}

