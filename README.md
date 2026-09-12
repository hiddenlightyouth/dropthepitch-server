# DropThePitch
우리는 항상 생각하죠, “이 아이디어 사용자들이 좋아할까?”
사용자에게 공개되기 전, 미리 사용자 의견을 들어보면 어떨까요?

<p align="center">
  <img src="https://github.com/hiddenlightyouth/dropthepitch-asset/blob/main/dropthepitch_github_thumbnail.png?raw=true" alt="apply-banner" width="100%" />
</p>

---

## 👨‍👩‍👧 팀원 소개
<div align="center">
    <table>
  <th><a href="https://github.com/Garden0728"> Garden0728 </th>
  <th><a href="https://github.com/minsu33"> 백민수 </th>
  <th><a href="https://github.com/yunh03"> 전윤환 </th>
    <tr>
        <td><img width="300" alt="Garden0728" src="https://avatars.githubusercontent.com/u/113418319?v=4"></td>
    <td><img width="300" alt="백민수" src="https://avatars.githubusercontent.com/u/257681691?v=4"></td>
        <td><img width="300" alt="전윤환" src="https://avatars.githubusercontent.com/u/57185499?v=4"></td>
    </tr>
<th> BE </th>
<th> BE </th>
<th> BE </th>
<tr>
<td>
- Gemini AI 태그 선별 호출로 연령대별 페르소나 매칭 구현<br>
- 분석 완료 후 페르소나 선별 비동기 연결, 선정 페르소나 교체 API 구현<br>
- AI 리포트 생성과 요약, 상세 리포트 조회 API 구현<br>
- 리포트 생성 프롬프트 및 응답 JSON 스키마 작성<br>
- 프로젝트, 파일, 분석, AI 사용량 엔티티 및 리포지토리 설계<br>
- 프로젝트 조회, 제목 변경 API 구현<br>
</td>
<td>
- Gemini AI 파일 분석 클라이언트와 문서, 이미지, 영상별 프롬프트 구현<br>
- 분석 응답 JSON 스키마 작성 및 업로드 후 분석 자동 실행 구현<br>
- PDF, 영상 썸네일 추출 구현<br>
- S3 파일 업로드, 다운로드 및 새 작업 시작 API 구현<br>
- AI 토큰 사용량 기록, 프로젝트 제목 자동 생성 구현<br>
- 의견 목록 연령대 필터, 정렬 조회 및 프로젝트 삭제 API 구현<br>
</td>
<td>
- 페르소나 30명 AI 의견 수집을 CompletableFuture와 전용 스레드 풀로 병렬 호출 처리<br>
- 수집 요청은 즉시 응답하고 백그라운드에서 처리하도록 설계, 타임아웃과 부분 실패 처리<br>
- 의견 수집 프롬프트와 응답 JSON 스키마 고도화, 자료 기반 의견 도출로 개선<br>
- 페르소나 엔티티, 리포지토리 설계 및 Redis 캐싱 적용<br>
- JWT 사용자 인증, 토큰 재발급 구현<br>
- 프로젝트 초기 세팅과 공통 응답, 예외 구조 설계<br>
- 사이드바 목록, 프로젝트 상태, 분석 결과 조회 API 구현<br>
</td>
</tr>
    </table>
</div>


---
## 🛠 Tech Stack

![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-8-02303A?logo=gradle&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F?logo=springboot&logoColor=white)
<br>
![Spring Security](https://img.shields.io/badge/Spring%20Security-7-6DB33F?logo=springsecurity&logoColor=white)
![JPA](https://img.shields.io/badge/Spring%20Data%20JPA-Hibernate-59666C?logo=hibernate&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-jjwt%200.12-000000?logo=jsonwebtokens&logoColor=white)
<br>
![MariaDB](https://img.shields.io/badge/MariaDB-003545?logo=mariadb&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?logo=redis&logoColor=white)
<br>
![Spring AI](https://img.shields.io/badge/Spring%20AI-2.0-6DB33F?logo=spring&logoColor=white)
![Gemini](https://img.shields.io/badge/Google%20Gemini-3.1%20Flash%20Lite-8E75B2?logo=googlegemini&logoColor=white)
<br>
![AWS S3](https://img.shields.io/badge/AWS%20S3-569A31?logo=amazons3&logoColor=white)
![FFmpeg](https://img.shields.io/badge/JavaCV%20%2F%20FFmpeg-007808?logo=ffmpeg&logoColor=white)
![PDFBox](https://img.shields.io/badge/Apache%20PDFBox-3.0-D22128?logo=apache&logoColor=white)
<br>
![Swagger](https://img.shields.io/badge/Swagger-springdoc%203.0-85EA2D?logo=swagger&logoColor=black)

---

## 주요 기능
- 아이디어가 담긴 문서, 이미지, 영상을 올리면 내용을 분석하고 썸네일을 만들어요
- 분석한 아이디어에 맞는 가상 사용자를 골라주고, 원하지 않으면 같은 연령대의 다른 사용자로 바꿀 수 있어요
- 가상 사용자들이 아이디어에 대한 의견을 남기고, 한 줄 요약과 상세 의견을 볼 수 있어요
- 모인 의견을 종합해 요약 리포트와 상세 리포트를 만들어요
- 진행한 작업은 사이드바에서 목록으로 관리하고, 분석과 의견 수집이 어디까지 됐는지 확인할 수 있어요

---

## 🧱 Module Structure

### Layered Architecture

```
// 프로젝트 전체 구조
├── src.main.java.kr.yuns.dropthepitchserver
│   ├── ai
│   ├── analyze
│   ├── common
│   ├── opinion
│   ├── persona
│   ├── project
│   ├── report
│   ├── user

// 도메인 패키지는 비즈니스 로직 담당
│   ├── analyze
│   │   └── controller
│   │   └── data
│   │   └── event
│   │   └── service
│   ├── opinion
│   │   └── config
│   │   └── controller
│   │   └── data
│   │   └── event
│   │   └── service
│   ├── persona
│   │   └── controller
│   │   └── data
│   │   └── event
│   │   └── service
│   ├── project
│   │   └── controller
│   │   └── data
│   │   └── event
│   │   └── service
│   ├── report
│   │   └── controller
│   │   └── data
│   │   └── event
│   │   └── service
│   ├── user
│   │   └── controller
│   │   └── data
│   │   └── service

// 인프라 관련 공통 기능 담당
│   ├── common
│   │   └── async
│   │   └── jpa
│   │   └── redis
│   │   └── response
│   │   └── s3
│   │   └── security
│   │   └── swagger

// AI 호출 공통 기능 담당
│   ├── ai
│   │   └── config
│   │   └── data
│   │   └── service
```

### AI Prompt / Schema

프롬프트와 응답 JSON 스키마는 코드에서 분리해 리소스로 관리해요.

```
src/main/resources
├── prompts
│   └── analysis-system.txt
│   └── analysis-user-document.txt
│   └── analysis-user-image.txt
│   └── analysis-user-video.txt
│   └── opinion-system.txt
│   └── report-system.txt
│   └── report-user.txt
│   └── tag-selection-system.txt
│   └── tag-selection-user.txt
├── schema
│   └── analysis-schema.json
│   └── opinion-schema.json
│   └── report-schema.json
│   └── tag-selection-schema.json
```

---

## ERD
![ERD](https://github.com/hiddenlightyouth/dropthepitch-asset/blob/main/dropthepitch_erd.png?raw=true)

---

## 🤝 협업

### Git Flow

> `feat` → `develop` → `main`

| Branch | Description |
| --- | --- |
| **main** | 모든 오류가 수정되어 배포가 가능한 branch |
| **develop** | 다음 배포를 위해 준비하는 branch |
| **feat** | 기능을 개발하는 branch |

- dev 브랜치에 머지할 때는 브랜치 간소화 및 revert 용이성을 위해 스쿼시 머지를 진행해요.

<br>

### Branch Naming

> `feat/{이슈번호}`

- 이슈는 이슈 트래커(GitHub Issues)에 기능 단위로 생성해요.
- 생성된 이슈 번호를 기반으로 branch를 생성해요.

<br>

## 🙏 Commit Convention
기능을 구현하고 Git에 커밋 시 아래의 태그를 반드시 사용해 주세요.

커밋 메시지에는 `Tag` + `구현한 기능` + `(#이슈번호)` 를 사용해 주세요.

| Tag | Description |
| --- | --- |
| **feat:** | 새 기능 추가 구현했을 때 |
| **fix:** | 버그 수정했을 때 |
| **docs:** | 문서 관련 수정했을 때 |
| **refactor:** | 코드 리팩토링했을 때 |
| **build:** | 빌드 관련 파일 수정했을 때 |
| **ci:** | CI 설정 관련 파일 수정했을 때 |
| **chore:** | 그 외 자잘한 수정을 했을 때 |
| **remove:** | 파일 삭제만 했을 때 |

<br>

## 📝 Pull Request & Issue Templates

PR 작성 및 Issue 생성 시 아래 템플릿을 활용해 주세요.

### Pull Request (PR)

#### 제목 양식
> `[Tag] 작업 내용`

| Tag | Description |
| --- | --- |
| **Feature** | 새 기능을 추가했을 때 |
| **Bug** | 버그를 수정했을 때 |
| **Refactor** | 리팩토링 했을 때 |

#### 본문 템플릿
```markdown
## 개요
- close #작업한_이슈_번호

## 작업사항
- 내용을 적어주세요.
```

<br>

### Issue Templates
Issue 생성 버튼을 누르면 템플릿이 등록되어 있으니 활용해 주세요.

#### 1. Feature
```markdown
## 💡 이슈 내용
- 기능 설명

## ✅ 상세 작업 내용
- [ ] 기능 1
- [ ] 기능 2
```

#### 2. Bug
```markdown
## 🐛 버그 내용
- [ ] 문제 상황

## 서버 로그 or 스크린샷
- 서버 로그 또는 사진 첨부
```

#### 3. Refactor
```markdown
## ⚙️ 리팩토링 내용
- [ ] 내용 1
- [ ] 내용 2
```

---