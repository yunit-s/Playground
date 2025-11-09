import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:photo_manager/photo_manager.dart';

class PhotoDetailBottomSheet extends StatelessWidget {
  final AssetEntity asset;

  const PhotoDetailBottomSheet({super.key, required this.asset});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: [
          const Text(
            '상세 정보',
            style: TextStyle(
              fontSize: 20,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 16),
          _buildInfoRow('파일명', asset.title ?? 'N/A'),
          _buildInfoRow('생성일', _formatDate(asset.createDateTime)),
          _buildInfoRow('수정일', _formatDate(asset.modifiedDateTime)),
          _buildInfoRow('크기', '${asset.width} x ${asset.height}'),
          if (asset.latitude != null && asset.longitude != null)
            _buildInfoRow('위치', 'Lat: ${asset.latitude}, Lng: ${asset.longitude}'),
          const SizedBox(height: 20),
        ],
      ),
    );
  }

  Widget _buildInfoRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            label,
            style: const TextStyle(
              fontWeight: FontWeight.w500,
              color: Colors.grey,
            ),
          ),
          Text(value),
        ],
      ),
    );
  }

  String _formatDate(DateTime dateTime) {
    return DateFormat('yyyy년 MM월 dd일 HH:mm').format(dateTime);
  }
}
