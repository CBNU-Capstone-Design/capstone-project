enum PostCategory {
  ALL('전체게시판'),
  POPULAR('인기게시판'),
  FREE_BOARD('자유게시판'),
  QUESTION_BOARD('질문게시판'),
  NOTICE('공지사항'),
  CLUB_COMMUNITY('동호회 커뮤니티');

  const PostCategory(this.displayName);
  final String displayName;

  String get value => name;
}