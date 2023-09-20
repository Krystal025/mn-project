package com.project.recipe.board.service;

import com.project.exception.CustomException.ImageMissingException;
import com.project.exception.CustomException.RequiredFieldMissingException;
import com.project.recipe.board.dao.BoardMapper;
import com.project.recipe.board.dto.BoardDto;
import com.project.recipe.image.sub.dao.SubImgMapper;
import com.project.recipe.image.sub.dto.SubImgDto;
import com.project.recipe.image.sub.service.SubImgService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardMapper rcpMapper;

    private final SubImgMapper subImgMapper;

    private final ImageUploadService imageUpload;

    private final SubImgService subImgService;

    //파일 업로드 경로
    @Value("${file.location}")
    private String imgPath;

    private String uploadAndInsertImage(BoardDto dto) {
        return imageUpload.uploadFile(dto.getImageFile());
    }

    //게시글 + 메인이미지 저장 처리
    @Override
    public void saveContent(BoardDto dto) {
        //필수 입력요소가 누락되었을 경우 예외 발생
        if (dto.getTitle() == null || dto.getCookingLevel() == null || dto.getIngredients() == null || dto.getContent() == null) {
            throw new RequiredFieldMissingException("필수입력요소가 누락되었습니다.");
        } else if (dto.getImageFile() == null) {
            throw new ImageMissingException("메인이미지가 누락되었습니다.");
        } else {
            String imagePath = uploadAndInsertImage(dto);
            dto.setMainPath(imagePath);
            //게시글 + 이미지 등록 (저장)
            rcpMapper.insertRcp(dto);
        }
    }

    //게시글 + 메인이미지 수정 처리
    @Override
    public void updateContent(BoardDto dto, List<MultipartFile> subImages) {
        try {
            //새로운 메인 이미지 가져오기
            MultipartFile newMainImg = dto.getImageFile();
            //새로운 이미지가 업로드 되었을 경우
            if (!newMainImg.isEmpty()) {
                String imagePath = uploadAndInsertImage(dto);
                dto.setMainPath(imagePath);
            }
            //게시글 수정
            rcpMapper.updateRcp(dto);
            SubImgDto subImgDto = new SubImgDto();
            //서브 이미지 수정
            if (!subImages.isEmpty()) {
                int rcpNum = dto.getRcpNum();
                //반복문 돌면서 삭제할 이미지 번호를 읽어오며 삭제
                for (int tmp : dto.getSubNums()) {
                    //기존 서브 이미지 삭제
                    subImgService.deleteImg(tmp);
                }
                //새로운 서브 이미지 저장
                subImgService.saveImg(rcpNum, subImages);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //게시글 삭제 처리 메소드
    @Override
    public void deleteContent(int rcpNum) {
        //게시글 번호로 해당 게시글 삭제
        rcpMapper.deleteRcp(rcpNum);
    }

    //전체 게시글 목록 조회 메소드
    @Override
    public List<BoardDto> getList(String keyword, String condition) {
        BoardDto dto = new BoardDto();
        //keyword가 있을 경우 검사
        if (keyword != null && !"".equals(keyword)) {
            //검색조건이 "작성자"인 경우
            if ("userNickname".equals(condition)) {
                //"작성자" 검색조건이 선택되었을 때 사용자가 입력한 검색키워드를 writer 필드에 저장
                dto.setUserNickname(keyword);
            }
        }
        //검색조건에 맞는 게시글 목록을 조회
        List<BoardDto> rcpList = rcpMapper.getList(dto);
        for (BoardDto recipe : rcpList) {
            //메인 이미지 파일 경로 생성 (이미지 경로 + 파일명)
            String mainPath = imgPath + File.separator + recipe.getMainSaveName();
            //이미지 파일 존재 여부 확인
            File imgFile = new File(mainPath);
            if (imgFile.exists()) {
                //이미지 파일이 존재하는 경우에만 레시피 객체에 이미지 경로 추가
                recipe.setMainPath(mainPath);
            }
        }
        //수정된 게시글 목록 반환
        return rcpList;
    }


    //게시글 목록 + 좋아요 조회 메소드
    @Override
    public List<BoardDto> getListWithLikes(String keyword, String condition, Integer userNum) {
        BoardDto dto = new BoardDto();
        dto.setUserNum(userNum);
        //keyword가 있을 경우 검사
        if (keyword != null && !"".equals(keyword)) {
            //검색조건이 "작성자"인 경우
            if ("userNickname".equals(condition)) {
                //"작성자" 검색조건이 선택되었을 때 사용자가 입력한 검색키워드를 writer 필드에 저장
                dto.setUserNickname(keyword);
            }
        }
        //검색조건에 맞는 게시글 목록을 조회
        List<BoardDto> rcpList = rcpMapper.getListWithLikes(dto);
        for (BoardDto recipe : rcpList) {
            //메인 이미지 파일 경로 생성 (이미지 경로 + 파일명)
            String mainPath = imgPath + File.separator + recipe.getMainSaveName();
            //이미지 파일 존재 여부 확인
            File imgFile = new File(mainPath);
            if (imgFile.exists()) {
                //이미지 파일이 존재하는 경우에만 레시피 객체에 이미지 경로 추가
                recipe.setMainPath(mainPath);
            }
        }
        //수정된 게시글 목록 반환
        return rcpList;
    }

    //게시글 상세 처리
    @Override
    public BoardDto getDetail(int rcpNum) {
        //게시글 상세정보 조회
        BoardDto rcpDetail = rcpMapper.getDetail(rcpNum);
        //게시글 번호로 서브 이미지들 조회
        List<SubImgDto> subImgList = subImgMapper.getImgs(rcpDetail.getRcpNum());
        //게시글 상세정보에 서브 이미지들 추가
        rcpDetail.setSubImgs(subImgList);
        //조회수 증가시키기
        rcpMapper.addViewCount(rcpNum);
        //서브 이미지를 추가한 게시글 상세정보 반환
        return rcpDetail;
    }

    //나의 게시글 조회 처리
    @Override
    public List<BoardDto> getMyList(int userNum) {
        //Dto객체에 사용자로부터 입력받은 userNum을 저장시킴
        BoardDto dto = new BoardDto();
        dto.setUserNum(userNum);
        //특정 사용가 작성한 게시글 목록을 조회
        List<BoardDto> myList = rcpMapper.getMyList(dto);
        //게시글 목록 반환
        return myList;
    }

    //카테고리 별 게시글 목록 조회 처리
    @Override
    public List<BoardDto> getByCategory(int petNum) {
        BoardDto dto = new BoardDto();
        dto.setPetNum(petNum);
        List<BoardDto> petList = rcpMapper.getByCategory(dto);
        //수정된 게시글 목록 반환
        return petList;
    }

}