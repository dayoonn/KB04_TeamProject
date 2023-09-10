package kb04.ditto.jgig.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import kb04.ditto.jgig.entity.QuizDto;
import kb04.ditto.jgig.mapper.QuizMapper;

@Controller
public class quizController {
	int ans = 2; // 정답 
	
	@Autowired
	private QuizMapper quizMapper;
	
	@GetMapping("/jgig/quiz")
	public String loadQuizData(Model model) throws IOException {
		// 퀴즈 풀었는지, 정답/오답인지 확인 
		String mem_id = "kb0002"; // 로그인한 아이디 넣기 
		QuizDto dto = quizMapper.selectQuiz(mem_id);
		if(dto != null) {
			model.addAttribute("quiz_stat", dto.getQuiz_stat());
			model.addAttribute("my_answer", dto.getMy_answer());
			model.addAttribute("answer", dto.getAnswer());
		} else {
			model.addAttribute("quiz_stat", "F");
		}
		
		
		
		String URL = "https://m.kbcapital.co.kr/cstmrPtct/fnncInfoSqre/fnncTmng.kbc";
		Document doc = Jsoup.connect(URL).data("targetRow","9").data("rowSize","3").get();
		Elements el = doc.select("ul[class=\"sp-accord nospace\"]");
		
		List<Map<String, String>> resultList = new LinkedList<Map<String, String>>();
		
		for(Element e: el.select("li")) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("tit", e.select("span[class=\"tit\"]").text());
			map.put("inner", e.select("div[class=\"inner\"]").text());
			resultList.add(map);
		}
		model.addAttribute("resultList", resultList);
		model.addAttribute("ans", ans);
		return "quiz/showQuiz";
	}
	 
	@PostMapping("/jgig/submitQuiz")
	@ResponseBody
	public QuizDto submitQuiz(@RequestParam("selectedOpt") int selectedOpt) {
		
		String mem_id = "kb0002"; // 로그인한 아이디 넣기 
		
		// 정답일 때 
		if(selectedOpt == ans) {
			quizMapper.insertQuiz(mem_id, "Y", ans, selectedOpt);
			// quizMapper.insertPoint(mem_id); // 포인트 insert 
			QuizDto dto = new QuizDto(mem_id, "Y", "", selectedOpt, ans);
			return dto; 
		} 
		
		quizMapper.insertQuiz(mem_id, "N", ans, selectedOpt);
		QuizDto dto = new QuizDto(mem_id, "N", "", selectedOpt, ans);

		return dto;
	}
	
	// 퀴즈데이터 크롤링 
	public void loadQuizData2(Model model) throws IOException {
		String URL = "https://m.kbcapital.co.kr/cstmrPtct/fnncInfoSqre/fnncTmng.kbc";
		Document doc = Jsoup.connect(URL).data("rowSize","10").get();
		Elements el = doc.select("ul[class=\"sp-accord nospace\"]");
		
		Map<String, String> resultMap = new HashMap<>();
		for(Element e: el.select("span[class=\"tit\"]")) {
			resultMap.put("tit", e.text());
			System.out.println(e.text());
		}
		for(Element e: el.select("div[class=\"inner\"]")) {
			resultMap.put("inner", e.text());
			System.out.println(e.text());
		}
		model.addAttribute("resultMap", resultMap);
	}
}
