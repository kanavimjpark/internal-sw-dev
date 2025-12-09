package com.example.a3dmodelsample

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a3dmodelsample.retrofit.GracenoteRepository
import com.example.a3dmodelsample.retrofit.RetrofitClient
import com.example.a3dmodelsample.retrofit.data.ProgramBundle

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RadioFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TheaterFragment : Fragment(R.layout.fragment_theater) {
    // TODO: Rename and change types of parameters

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GenreAdapter
    private val apiKey = "b2484d39ae469be167d2f47ffa5eb2222684a3167b79c16c42ba233aa0808448"

    private lateinit var btnAll: AppCompatButton
    private lateinit var btnDisney: AppCompatButton
    private lateinit var btnNetflix: AppCompatButton
    private lateinit var btnHulu: AppCompatButton
    private lateinit var btnAmazon: AppCompatButton
    private lateinit var btnGroup: LinearLayout
    private var netflixUrl = ""
    private var huluUrl = ""
    private var disneyUrl = ""
    private var primeUrl = ""
    private var paramountplusUrl = ""
    private var selectedGenreType = ""
    private lateinit var bundleLists : List<ProgramBundle>
    private var isFirst = true;

    private val buttonList = mutableListOf<AppCompatButton>()

    private val viewModel: ProgramListViewModel by viewModels {
        val api = RetrofitClient.createGraceNote(apiKey)
        val repository = GracenoteRepository(api)
        ProgramListViewModelFactory(repository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isFirst = true
        btnAll = view.findViewById(R.id.btnAll)
        btnDisney = view.findViewById(R.id.btnDisney)
        btnNetflix = view.findViewById(R.id.btnNetflix)
        btnHulu = view.findViewById(R.id.btnHULU)
        btnAmazon = view.findViewById(R.id.btnAmazon)
        btnGroup = view.findViewById(R.id.btnGroup)

        btnGroup.visibility = View.VISIBLE

        buttonList.addAll(listOf(btnAll, btnDisney, btnNetflix, btnHulu, btnAmazon))

        buttonList.forEach { button ->
            button.setOnClickListener {
                updateSelectedButton(button)
            }
        }

        updateSelectedButton(btnAll)

        recyclerView = view.findViewById(R.id.rv_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.loadBundles()

        viewModel.bundles.observe(viewLifecycleOwner) { bundles ->
            makeMovieList(bundles)
            bundleLists = bundles
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
            if (paramountplusUrl.isNotEmpty()) links["paramountplus"] = paramountplusUrl

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
            huluUrl = programUrls.firstOrNull { it.contains("hulu", ignoreCase = true) }.orEmpty()
            disneyUrl = programUrls.firstOrNull { it.contains("disneyplus", ignoreCase = true) }.orEmpty()
            primeUrl = programUrls.firstOrNull { it.contains("primevideo", ignoreCase = true) }.orEmpty()
            paramountplusUrl = programUrls.firstOrNull { it.contains("paramountplus", ignoreCase = true) }.orEmpty()
        }

    }

    private fun updateSelectedButton(selected: AppCompatButton) {
        buttonList.forEach { it.isSelected = false }
        selected.isSelected = true
        selectedGenreType = selected.text.toString()
        Log.d("mjpark", selectedGenreType)
        if(!isFirst){
            makeMovieList(bundleLists)
        }
    }

    private fun makeMovieList(bundles: List<ProgramBundle>){
        adapter = GenreAdapter(bundles,
            selectedGenreType,
            onItemClick = { programID ->
                Log.d("mjpark", "Fragment received programID: $programID")
                viewModel.loadProgram(programID)
            },
            onArrowClick = { genreName ->
                Log.d("mjpark", "▶ 클릭된 장르: $genreName")

                val fragment = GenreDetailFragment.newInstance(genreName, selectedGenreType)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.mediaTabContent, fragment)
                    .addToBackStack(null)
                    .commit()
                viewModel.clearSelectedPrograms()
            })
        recyclerView.adapter = adapter
        isFirst = false
    }

}