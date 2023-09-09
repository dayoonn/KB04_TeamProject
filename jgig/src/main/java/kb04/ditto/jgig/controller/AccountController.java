package kb04.ditto.jgig.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import kb04.ditto.jgig.entity.AccountDto;
import kb04.ditto.jgig.mapper.AccountMapper;


@Controller
public class AccountController {

	@Autowired
	AccountMapper accountMapper;
	
	//open_form1에서 입력받은 ssn, phone_num, act_name, mem_nm을 open_form2에서도 사용하기 위한 전역변수 설정
	String ssn, phone_num, actName, memName, mem_id;

	@GetMapping("/jgig/login_test")
	public String loginTest(HttpSession session) {
		// 로그인 정보를 세션에 저장합니다.
		session.setAttribute("loggedIn", true);
		session.setAttribute("mem_id", "kb0002"); // 테스트용 아이디
		session.setAttribute("mem_nm", "김철수"); // 테스트용 이름
		session.setAttribute("phone_num", "01054237895"); // 테스트용 폰번호
		session.setAttribute("ssn", "990101-0000000"); // 테스트용 주민번호

		// 로그인 정보를 저장한 후, 다음 페이지로 리다이렉트합니다.
		return "redirect:/jgig/open_account1"; // 로그인 후의 페이지로 리다이렉트
	}
	
	// 테스트용 로그아웃 처리 로직
	@GetMapping("/jgig/logout")
	public String logoutTest(HttpSession session) {
		if (session != null) {
			session.invalidate();
		}

		return "redirect:/jgig/open_account1"; // 로그인 후의 페이지로 리다이렉트
	}

	// 로그인 체크 함수
	public String login_check(HttpSession session) {
		// 세션에서 로그인 정보를 확인
		Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");

		// 로그인 여부를 확인하고, 로그인되지 않은 경우 로그인 페이지로 리다이렉트
		if (loggedIn == null || !loggedIn) {
			return "redirect:/jgig/login"; // 로그인 페이지로 리다이렉트합니다.
		}
		// 로그인된 경우, 세션에서 로그인 아이디를 받아옵니다.
		String mem_id = (String) session.getAttribute("mem_id");
		return mem_id;
	}
	

	// 계좌개설폼1단계
	@GetMapping("/jgig/open_account1")
	public String open_form1() {
		return "account/open_account1";
	}
	
	// 계좌개설폼2단계
	@PostMapping("/jgig/open_account2")
	public String open_form2(Model model) {
		model.addAttribute("act_name", actName);
		model.addAttribute("mem_nm", memName);
		model.addAttribute("ssn", ssn);
		model.addAttribute("phone_num", phone_num);
		model.addAttribute("mem_id", mem_id);

		return "account/open_account2";
	}
	
	
	//본인확인을 위한 회원 체크
	@PostMapping("/jgig/member_check")
	@ResponseBody
	public Map<String, Object> member_check(@RequestParam("ssn1") String ssn1, @RequestParam("ssn2") String ssn2,
			@RequestParam("act_name") String act_name, @RequestParam("mem_nm") String mem_nm,
			@RequestParam("phone_num1") String phone_num1, @RequestParam("phone_num2") String phone_num2,
			@RequestParam("phone_num3") String phone_num3, HttpSession session, Model model) {
		Map<String, Object> response = new HashMap<>();

		actName = act_name;
		ssn = ssn1 + "-" + ssn2;
		phone_num = phone_num1 + phone_num2 + phone_num3;
		memName = mem_nm;
		
		String returnVal = login_check(session);
        if (returnVal.equals("redirect:/jgig/login"))
        	notLogin();
        mem_id = returnVal;
        
        
		String mem_id = (String) session.getAttribute("mem_id"); //login_check(session);
		String mem_phone_num = (String) session.getAttribute("phone_num");
		String mem_ssn = (String) session.getAttribute("ssn");
		
		if (mem_id != null) {
            // 사용자 정보 가져오기
            String mem_name = accountMapper.findById(mem_id);
			  if(mem_nm.equals(mem_name) && mem_ssn.equals(ssn) && mem_phone_num.equals(phone_num)){
				 response.put("success", true);		
			  }
        	  else {
				 response.put("success", false);
        	  }
		}
		return response;
	}
	
	//로그인체크
	private String notLogin() {
		return "redirect:/jgig/open_account1";
	}

	//계좌개설을 위한 두단계 form의 입력값들을 db에 저장하기 위한 action메서드
	@PostMapping("/jgig/open_action")
	public String open_action(AccountDto dto, Model model) {
		accountMapper.insert(dto);
		long account_num = accountMapper.account_num(dto);
		model.addAttribute("dto", dto);
		model.addAttribute("account_num", account_num);
		model.addAttribute("msg", "해당 계좌의 개설이 완료되었습니다");
		return "account/open_ok";
	}

	// 계좌목록조회
	@GetMapping("/jgig/account_list")
	public String account_list(Model model, HttpSession session) {
		String mem_id = (String) session.getAttribute("mem_id");
		List<AccountDto> list = accountMapper.list(mem_id);
		if(list.size() == 0 ) {
			return "account/no_account";
		}
		int totalBalance = accountMapper.totalBalance(mem_id);
		
		model.addAttribute("totalBalance", totalBalance);
		model.addAttribute("account_list", list);
		return "account/list";
	}

	// 계좌관리
	@GetMapping("/jgig/account_management")
	public String account_management(Model model, @RequestParam("account") long account) {
		AccountDto dto = accountMapper.findByAccount(account);
		Date regdate = dto.getRegdate();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
		String formattedRegdate = dateFormat.format(regdate);

		model.addAttribute("formattedRegdate", formattedRegdate);
		model.addAttribute("dto", dto);
		return "account/management";
	}
	
	//계좌 비밀번호 수정 폼
	@GetMapping("/jgig/update_password")
	public String board_update_form(Model model , @RequestParam("account") long account) {
		AccountDto dto = accountMapper.findByAccount(account);
		model.addAttribute("dto",dto);
		return "account/update_password";
	}
	
	//계좌 비밀번호 수정 액션
	@PostMapping("/jgig/update_password_action")
	public String update_password_action(AccountDto dto, Model model) {
		accountMapper.update(dto);
		model.addAttribute("dto", dto);
		model.addAttribute("msg","비밀번호 수정이 완료되었습니다.");
		return "account/update_pw_ok";
	}
	
	//계좌해지 폼
	@GetMapping("/jgig/termination")
	public String termination(@RequestParam("account") long account, Model model) {
		AccountDto dto = accountMapper.findByAccount(account);
		Date regdate = dto.getRegdate();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
		String formattedRegdate = dateFormat.format(regdate);
		model.addAttribute("formattedRegdate", formattedRegdate);
		model.addAttribute("dto", dto);
		return "account/termination";
	}
	
	//계좌해지 액션
	@PostMapping("/jgig/termination_action")
	public String termination_action(RedirectAttributes redirect,@RequestParam("account") long account,@RequestParam("act_password") int pw, Model model, AccountDto dto) {
		int checkPw = accountMapper.checkPw(account);
		if(checkPw == pw) {
			accountMapper.terminate(dto);
			model.addAttribute("msg", "계좌 해지가 완료되었습니다.");
			return "account/termination_ok";
		}
		redirect.addFlashAttribute("msg", "비밀번호가 틀립니다. 다시한번 확인해주세요.");
		
		return "redirect:/jgig/termination?account="+account;
	}
}
