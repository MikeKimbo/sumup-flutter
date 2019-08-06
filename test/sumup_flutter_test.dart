import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:sumup_flutter/sumup_flutter.dart';

void main() {
  const MethodChannel channel = MethodChannel('sumup_flutter');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

}
