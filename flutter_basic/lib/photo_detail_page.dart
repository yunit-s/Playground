import 'package:flutter/material.dart';
import 'package:flutter_basic/photo_detail_bottom_sheet.dart';
import 'package:photo_manager/photo_manager.dart';
import 'package:photo_manager_image_provider/photo_manager_image_provider.dart';

class PhotoDetailPage extends StatefulWidget {
  final List<AssetEntity> photos;
  final int initialIndex;

  const PhotoDetailPage({
    super.key,
    required this.photos,
    required this.initialIndex,
  });

  @override
  State<PhotoDetailPage> createState() => _PhotoDetailPageState();
}

class _PhotoDetailPageState extends State<PhotoDetailPage> {
  late final PageController _pageController;
  late int _currentIndex;

  @override
  void initState() {
    super.initState();
    _currentIndex = widget.initialIndex;
    _pageController = PageController(initialPage: widget.initialIndex);
  }

  void _onPageChanged(int index) {
    setState(() {
      _currentIndex = index;
    });
  }

  void _onSwipeDown() {
    Navigator.of(context).pop();
  }

  void _onSwipeUp() {
    final asset = widget.photos[_currentIndex];
    showModalBottomSheet(
      context: context,
      builder: (context) => PhotoDetailBottomSheet(asset: asset),
      isScrollControlled: true,
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        iconTheme: const IconThemeData(color: Colors.white),
      ),
      body: GestureDetector(
        onVerticalDragEnd: (details) {
          // Detect swipe down or up
          if (details.primaryVelocity! > 200) {
            _onSwipeDown();
          } else if (details.primaryVelocity! < -200) {
            _onSwipeUp();
          }
        },
        child: PageView.builder(
          controller: _pageController,
          itemCount: widget.photos.length,
          onPageChanged: _onPageChanged,
          itemBuilder: (context, index) {
            final asset = widget.photos[index];
            return Center(
              child: AssetEntityImage(
                asset,
                isOriginal: true, // Load high-quality image
                fit: BoxFit.contain,
                errorBuilder: (context, error, stackTrace) {
                  return const Center(
                    child: Icon(
                      Icons.error,
                      color: Colors.red,
                      size: 48,
                    ),
                  );
                },
              ),
            );
          },
        ),
      ),
    );
  }
}
