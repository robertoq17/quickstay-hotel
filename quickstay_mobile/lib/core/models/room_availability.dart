/// Espeja com.quickstay.inventory.dto.RoomAvailabilityResponse
class RoomAvailability {
  final String roomId;
  final String hotelName;
  final String city;
  final String roomType;
  final double pricePerNight;
  final int capacity;

  RoomAvailability({
    required this.roomId,
    required this.hotelName,
    required this.city,
    required this.roomType,
    required this.pricePerNight,
    required this.capacity,
  });

  factory RoomAvailability.fromJson(Map<String, dynamic> json) {
    return RoomAvailability(
      roomId: json['roomId'] as String,
      hotelName: json['hotelName'] as String,
      city: json['city'] as String,
      roomType: json['roomType'] as String,
      pricePerNight: (json['pricePerNight'] as num).toDouble(),
      capacity: json['capacity'] as int,
    );
  }
}
