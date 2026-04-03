import 'package:geocoding/geocoding.dart';
import 'package:geolocator/geolocator.dart';

class LocationResult {
  LocationResult({
    required this.latitude,
    required this.longitude,
    required this.street,
    required this.neighborhood,
    required this.postalCode,
  });

  final double latitude;
  final double longitude;
  final String street;
  final String neighborhood;
  final String postalCode;
}

class LocationService {
  Future<LocationResult> fetchAddress() async {
    final serviceEnabled = await Geolocator.isLocationServiceEnabled();
    if (!serviceEnabled) {
      throw Exception('Ative o GPS para continuar.');
    }

    LocationPermission permission = await Geolocator.checkPermission();
    if (permission == LocationPermission.denied) {
      permission = await Geolocator.requestPermission();
    }

    if (permission == LocationPermission.denied ||
        permission == LocationPermission.deniedForever) {
      throw Exception('Permissão de localização negada.');
    }

    final position = await Geolocator.getCurrentPosition();
    final places = await placemarkFromCoordinates(
      position.latitude,
      position.longitude,
      localeIdentifier: 'pt_BR',
    );

    final place = places.isEmpty ? null : places.first;

    return LocationResult(
      latitude: position.latitude,
      longitude: position.longitude,
      street: place?.street ?? '',
      neighborhood: place?.subLocality ?? '',
      postalCode: place?.postalCode ?? '',
    );
  }
}
