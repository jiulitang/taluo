package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

enum class TarotArcana(val labelZh: String) {
  MAJOR("大阿卡那"),
  MINOR("小阿卡那"),
}

enum class TarotSuit(val labelZh: String, val elementZh: String) {
  MAJOR("大阿卡那", "以太/灵魂"),
  WANDS("权杖", "火元素"),
  CUPS("圣杯", "水元素"),
  SWORDS("宝剑", "风元素"),
  PENTACLES("星币", "土元素"),
}

@Entity(tableName = "tarot_cards")
data class TarotCardEntity(
  @PrimaryKey val id: Int,
  val number: Int,
  val arcana: String, // "MAJOR" or "MINOR"
  val suit: String, // "MAJOR", "WANDS", "CUPS", "SWORDS", "PENTACLES"
  val nameZh: String,
  val nameEn: String,
  val romanNumeral: String,
  val element: String,
  val astrology: String,
  val keywordsUpright: String,
  val keywordsReversed: String,
  val meaningUpright: String,
  val meaningReversed: String,
  val loveAdvice: String,
  val careerAdvice: String,
  val spiritualAdvice: String,
  val customNotes: String = "",
  val aiRefinedAt: Long? = null,
  val isCustomUpdated: Boolean = false,
)

@Entity(tableName = "mentor_personas")
data class MentorPersonaEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val title: String,
  val avatarKey: String = "seraphina",
  val personality: String,
  val readingStyle: String,
  val systemPrompt: String,
  val greeting: String,
  val isActive: Boolean = false,
  val isDefault: Boolean = false,
  val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "worldbook_entries")
data class WorldbookEntryEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val keywords: String, // Comma-separated trigger keywords
  val content: String,
  val sourceName: String = "内置圣典",
  val isAlwaysActive: Boolean = false,
  val isEnabled: Boolean = true,
  val updatedAt: Long = System.currentTimeMillis(),
)

@JsonClass(generateAdapter = true)
data class DrawnCardSnapshot(
  val cardId: Int,
  val cardNameZh: String,
  val cardNameEn: String,
  val romanNumeral: String,
  val suit: String,
  val isReversed: Boolean,
  val positionIndex: Int,
  val positionName: String,
  val positionMeaning: String,
)

@Entity(tableName = "reading_records")
data class ReadingRecordEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val dateKey: String,
  val isDailyDraw: Boolean,
  val spreadId: String,
  val spreadName: String,
  val question: String,
  val drawnCardsJson: String,
  val aiSpreadSelectionReason: String = "",
  val aiInterpretation: String = "",
  val mentorName: String = "",
  val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val personaId: Long,
  val role: String, // "user" or "assistant"
  val content: String,
  val attachedReadingSummary: String = "",
  val timestamp: Long = System.currentTimeMillis(),
)

data class SpreadPosition(
  val index: Int,
  val title: String,
  val description: String,
)

data class TarotSpread(
  val id: String,
  val nameZh: String,
  val nameEn: String,
  val category: String,
  val cardCount: Int,
  val description: String,
  val bestFor: String,
  val positions: List<SpreadPosition>,
)

object BuiltInSpreads {
  val all: List<TarotSpread> =
    listOf(
      TarotSpread(
        id = "single_sanctuary",
        nameZh = "圣光一牌",
        nameEn = "Sacred Single",
        category = "日常指引",
        cardCount = 1,
        description = "大道至简的圣光启示，聚焦当下最核心的能量与直接答案。",
        bestFor = "每日指引、是/否倾向、当下最需要觉察的一件事",
        positions = listOf(SpreadPosition(0, "圣光核心启示", "当下能量中心与宇宙最想传达的讯息")),
      ),
      TarotSpread(
        id = "time_flow",
        nameZh = "圣杯三元 · 时间流",
        nameEn = "Timeline Trinity",
        category = "通用因果",
        cardCount = 3,
        description = "顺着时间长河观照过去成因、当下课题与未来趋势。",
        bestFor = "事件发展脉络、现状梳理、近期趋势预测",
        positions =
          listOf(
            SpreadPosition(0, "过去根源", "塑造当前局面的前因与过往能量"),
            SpreadPosition(1, "当下显化", "此刻正在发生的核心课题与状态"),
            SpreadPosition(2, "未来流向", "若保持当前轨迹即将迎来的结果"),
          ),
      ),
      TarotSpread(
        id = "mind_body_spirit",
        nameZh = "身心灵圣殿",
        nameEn = "Mind · Body · Spirit",
        category = "自我觉察",
        cardCount = 3,
        description = "照见思绪、现实行动与高我灵魂之间的共振与失衡。",
        bestFor = "情绪内耗、自我成长、灵性觉醒与内在平衡",
        positions =
          listOf(
            SpreadPosition(0, "心智思绪", "理智念头、担忧或信念模式"),
            SpreadPosition(1, "现实行动", "物质层面、身体健康与日常行动力"),
            SpreadPosition(2, "灵性指引", "高我智慧与灵魂深处的召唤"),
          ),
      ),
      TarotSpread(
        id = "venus_love",
        nameZh = "维纳斯之镜",
        nameEn = "Venus Mirror",
        category = "情感关系",
        cardCount = 5,
        description = "如澄净圣镜般映照双方心意、羁绊课题与情感走向。",
        bestFor = "恋爱关系、暧昧互动、复合或深层人际羁绊",
        positions =
          listOf(
            SpreadPosition(0, "你的心境", "你在这段关系中的真实感受与期待"),
            SpreadPosition(1, "对方视角", "对方内心对你的态度与潜意识想法"),
            SpreadPosition(2, "圣洁羁绊", "连接彼此的核心缘分与互动能量"),
            SpreadPosition(3, "隐匿阻碍", "需要温柔化解的误解或课题"),
            SpreadPosition(4, "关系远景", "这段关系的近期发展与建议方向"),
          ),
      ),
      TarotSpread(
        id = "crossroads_choice",
        nameZh = "抉择之门",
        nameEn = "Gate of Choice",
        category = "决策抉择",
        cardCount = 5,
        description = "在人生岔路口对比两条道路的过程与最终回响。",
        bestFor = "换工作/留任、城市选择、二选一重大决定",
        positions =
          listOf(
            SpreadPosition(0, "十字路口中心", "你的本质立场与抉择核心动机"),
            SpreadPosition(1, "选择 A · 过程", "走上第一条道路会经历的氛围与考验"),
            SpreadPosition(2, "选择 A · 归宿", "第一条道路带来的成长与结果"),
            SpreadPosition(3, "选择 B · 过程", "走上第二条道路会经历的氛围与考验"),
            SpreadPosition(4, "选择 B · 归宿", "第二条道路带来的成长与结果"),
          ),
      ),
      TarotSpread(
        id = "celtic_cross",
        nameZh = "凯尔特圣十字",
        nameEn = "Celtic Cross",
        category = "深度全览",
        cardCount = 10,
        description = "古典而庄严的十牌全景圣阵，剖析命运纵深与外在助缘。",
        bestFor = "复杂人生议题、年度运势、事业全局与深层因果探索",
        positions =
          listOf(
            SpreadPosition(0, "核心现状", "当前处境的中心主题"),
            SpreadPosition(1, "交叉挑战", "横亘在面前的助力或阻力"),
            SpreadPosition(2, "潜意识根基", "深藏心底的渴望与根源"),
            SpreadPosition(3, "过往回响", "正在退去但仍有影响的过去"),
            SpreadPosition(4, "高空愿景", "理想目标与可能达到的高度"),
            SpreadPosition(5, "近期转折", "即将步入视野的新能量"),
            SpreadPosition(6, "自我姿态", "你在这件事中的内在力量"),
            SpreadPosition(7, "外在环境", "身边人事物与外在局势"),
            SpreadPosition(8, "希望与畏惧", "期待与焦虑交织的暗流"),
            SpreadPosition(9, "最终启示", "十牌汇聚而成的命运归宿"),
          ),
      ),
    )

  fun findById(id: String): TarotSpread = all.firstOrNull { it.id == id } ?: all.first()
}
