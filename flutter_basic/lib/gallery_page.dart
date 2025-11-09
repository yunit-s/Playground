import 'package:flutter/material.dart';
import 'package:flutter_basic/album_detail_page.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:photo_manager/photo_manager.dart';

class GalleryPage extends StatefulWidget {
  const GalleryPage({super.key});

  @override
  State<GalleryPage> createState() => _GalleryPageState();
}

class _GalleryPageState extends State<GalleryPage> {
  List<AssetPathEntity> _albums = [];
  bool _isLoading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _requestPermissionAndFetchAlbums();
  }

  Future<void> _requestPermissionAndFetchAlbums() async {
    setState(() {
      _isLoading = true;
    });

    // Request permissions
    final status = await Permission.photos.request();

    if (status.isGranted) {
      try {
        // Fetch albums
        final albums = await PhotoManager.getAssetPathList(
          type: RequestType.image, // We only want images for now
        );
        setState(() {
          _albums = albums;
          _isLoading = false;
        });
      } catch (e) {
        setState(() {
          _error = e.toString();
          _isLoading = false;
        });
      }
    } else {
      setState(() {
        _error = '사진 접근 권한이 거부되었습니다. 앱 설정에서 권한을 허용해주세요.';
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: _buildBody(),
    );
  }

  Widget _buildBody() {
    if (_isLoading) {
      return const Center(child: CircularProgressIndicator());
    }

    if (_error != null) {
      return Center(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text(
                _error!,
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 16),
              ElevatedButton(
                onPressed: () {
                  openAppSettings();
                },
                child: const Text('설정으로 이동'),
              )
            ],
          ),
        ),
      );
    }

    if (_albums.isEmpty) {
      return const Center(child: Text('앨범이 없습니다.'));
    }

    // For now, just display album names in a ListView
    return ListView.builder(
      itemCount: _albums.length,
      itemBuilder: (context, index) {
        final album = _albums[index];
        return ListTile(
          title: Text(album.name),
          subtitle: FutureBuilder<int>(
            future: album.assetCountAsync,
            builder: (context, snapshot) {
              if (snapshot.connectionState == ConnectionState.done && snapshot.hasData) {
                return Text('사진 ${snapshot.data}장');
              }
              return const Text('사진 수 로딩 중...');
            },
          ),
          onTap: () {
            Navigator.push(
              context,
              MaterialPageRoute(
                builder: (context) => AlbumDetailPage(album: album),
              ),
            );
          },
        );
      },
    );
  }
}
