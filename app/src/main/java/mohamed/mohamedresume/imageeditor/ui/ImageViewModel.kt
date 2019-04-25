package mohamed.mohamedresume.imageeditor.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import mohamed.mohamedresume.imageeditor.data.ImageRepository

class ImageViewModel : ViewModel() {

    private val _imageLiveData = MutableLiveData<List<String>>()
//    private val repository = ImageRepository()
//    val imgaeLiveData = Transformations.switchMap(_imageLiveData) {
//        repository.getImages()
//    }

    fun updateImages(pathList: List<String>) {
        _imageLiveData.value = pathList
    }
}