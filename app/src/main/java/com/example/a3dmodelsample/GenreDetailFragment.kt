package com.example.a3dmodelsample

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a3dmodelsample.retrofit.data.BundleProgram
import com.example.a3dmodelsample.retrofit.GracenoteRepository
import com.example.a3dmodelsample.retrofit.RetrofitClient


class GenreDetailFragment : Fragment(R.layout.fragment_genre_detail) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DetailAdapter
    private lateinit var tvTitle : TextView
    private lateinit var btnBack : ImageView

    private var netflixUrl = ""
    private var hbomaxUrl = ""
    private var huluUrl = ""
    private var disneyUrl = ""
    private var primeUrl = ""
    private var paramountplusUrl = ""
    private val apiKey = "b2484d39ae469be167d2f47ffa5eb2222684a3167b79c16c42ba233aa0808448"

    private val buttonList = mutableListOf<AppCompatButton>()

    companion object {
        private const val ARG_GENRE_NAME = "genre_name"
        private const val ARG_GENRE_TYPE = "genre_type"
        fun newInstance(genreName: String, type: String) = GenreDetailFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_GENRE_NAME, genreName)
                putString(ARG_GENRE_TYPE, type)
            }
        }
    }

    private val genreName by lazy {
        requireArguments().getString(ARG_GENRE_NAME).orEmpty()
    }

    private val genreType by lazy {
        requireArguments().getString(ARG_GENRE_TYPE).orEmpty()
    }

    private val viewModel: ProgramListViewModel by viewModels {
        val api = RetrofitClient.createGraceNote(apiKey)
        val repository = GracenoteRepository(api)
        ProgramListViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("mjpark","genre name222 : $genreName")
        Log.d("mjpark","genre type222 : $genreType")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvTitle = view.findViewById(R.id.tvTitle)
        btnBack = view.findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        recyclerView = view.findViewById(R.id.rv_detail)

        tvTitle.text = genreName

        viewModel.loadBundles()


        viewModel.bundles.observe(viewLifecycleOwner) { bundles ->

            val targetName = genreName

            val programsForGenre: List<BundleProgram> = when (genreType) {
                "Disney+" -> bundles.firstOrNull { it.name == targetName }?.programs.orEmpty().filter { it.availableOn?.firstOrNull()?.catalog?.name?.equals("Disney+ US")!! }

                "Netflix" -> bundles.firstOrNull { it.name == targetName }?.programs.orEmpty().filter { it.availableOn?.firstOrNull()?.catalog?.name?.equals("Netflix")!! }

                "HULU" -> bundles.firstOrNull { it.name == targetName }?.programs.orEmpty().filter { it.availableOn?.firstOrNull()?.catalog?.name?.equals("Hulu US")!! }

                "Amazon Prime Video" -> bundles.firstOrNull { it.name == targetName }?.programs.orEmpty().filter { it.availableOn?.firstOrNull()?.catalog?.name?.equals("Amazon PV US")!! }
//                    "Paramount" -> bundle.programs.filter { it.availableOn?.firstOrNull()?.catalog?.name.equals("Paramount Plus US") }
                else -> bundles.firstOrNull { it.name == targetName }?.programs.orEmpty()
            }

            adapter = DetailAdapter(programsForGenre,
                onItemClick = { programID ->
                    Log.d("mjpark", "Fragment received programID: $programID")
                    viewModel.loadProgram(programID)
                })
            recyclerView.layoutManager = GridLayoutManager(context, 4 , GridLayoutManager.VERTICAL, false)
            recyclerView.adapter = adapter
        }

        viewModel.selectedPrograms.observe(viewLifecycleOwner) { program ->

            val firstProgram = program.firstOrNull()  // 리스트가 비어있으면 null
            val bannerUrl = firstProgram?.images?.firstOrNull { it.orientation == "LANDSCAPE" }?.URL
            val title = firstProgram?.name
            val summary = firstProgram?.description

            viewModel.loadProgramUrls()

            val links = mutableMapOf<String, String>()
            if (netflixUrl.isNotEmpty()) links["netflix"] = netflixUrl
            if (huluUrl.isNotEmpty()) links["hulu"] = huluUrl
            if (disneyUrl.isNotEmpty()) links["disney"] = disneyUrl
            if (primeUrl.isNotEmpty()) links["prime"] = primeUrl

            if (firstProgram != null) {
                val dialog = context?.let { ctx ->
                    TheaterPopupDialog(ctx)
                        .setBanner(bannerUrl ?: "")  // null이면 빈 문자열 처리
                        .setTitleText(title ?: "")
                        .setMessageText("$")
                        .setSummaryText(summary ?: "")
                        .setStreamingLinks(links)
                }

                dialog?.show()
            } else {
                Log.w("VideoFragment", "선택된 프로그램이 없습니다.")
            }
        }

        viewModel.videoLink.observe(viewLifecycleOwner){programUrls ->

            netflixUrl = programUrls.firstOrNull { it.contains("netflix", ignoreCase = true) }.orEmpty()
            hbomaxUrl = programUrls.firstOrNull { it.contains("netflix", ignoreCase = true) }.orEmpty()
            huluUrl = programUrls.firstOrNull { it.contains("hulu", ignoreCase = true) }.orEmpty()
            disneyUrl = programUrls.firstOrNull { it.contains("disneyplus", ignoreCase = true) }.orEmpty()
            primeUrl = programUrls.firstOrNull { it.contains("primevideo", ignoreCase = true) }.orEmpty()
            paramountplusUrl = programUrls.firstOrNull { it.contains("paramountplus", ignoreCase = true) }.orEmpty()

        }
    }
}