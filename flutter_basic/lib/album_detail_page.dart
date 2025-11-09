import 'package:flutter/material.dart';
import 'package:flutter_basic/photo_detail_page.dart';
import 'package:photo_manager/photo_manager.dart';
import 'package:photo_manager_image_provider/photo_manager_image_provider.dart';

class AlbumDetailPage extends StatefulWidget {
  final AssetPathEntity album;

  const AlbumDetailPage({super.key, required this.album});

  @override
  State<AlbumDetailPage> createState() => _AlbumDetailPageState();
}

class _AlbumDetailPageState extends State<AlbumDetailPage> {
  List<AssetEntity> _photos = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _fetchPhotos();
  }

  Future<void> _fetchPhotos() async {
    setState(() {
      _isLoading = true;
    });

    final photos = await widget.album.getAssetListPaged(page: 0, size: 100);

    setState(() {
      _photos = photos;
      _isLoading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.album.name),
      ),
      body: _buildBody(),
    );
  }

  Widget _buildBody() {
    if (_isLoading) {
      return const Center(child: CircularProgressIndicator());
    }

    if (_photos.isEmpty) {
      return const Center(child: Text('사진이 없습니다.'));
    }

    return GridView.builder(
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 3, // 3 columns
        crossAxisSpacing: 4.0,
        mainAxisSpacing: 4.0,
      ),
      itemCount: _photos.length,
      itemBuilder: (context, index) {
        final asset = _photos[index];
        return GestureDetector(
          onTap: () {
            Navigator.push(
              context,
              MaterialPageRoute(
                builder: (context) => PhotoDetailPage(
                  photos: _photos,
                  initialIndex: index,
                ),
              ),
            );
          },
          child: Image(
            image: AssetEntityImageProvider(
              asset,
              isOriginal: false, // Load thumbnail
              thumbnailSize: const ThumbnailSize.square(200),
            ),
            fit: BoxFit.cover,
            errorBuilder: (context, error, stackTrace) {
              return const Center(
                child: Icon(
                  Icons.error,
                  color: Colors.red,
                ),
              );
            },
          ),
        );
      },
    );
  }
}