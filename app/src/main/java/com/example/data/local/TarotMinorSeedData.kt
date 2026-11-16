package com.example.data.local

import com.example.data.model.TarotCardEntity

object TarotMinorSeedData {
  private data class RankSpec(
    val number: Int,
    val roman: String,
    val nameZhSuffix: String,
    val nameEnPrefix: String,
    val kwUp: String,
    val kwRev: String,
    val themeUp: String,
    val themeRev: String,
  )

  private val ranks =
    listOf(
      RankSpec(1, "I", "王牌", "Ace of", "纯粹种子、全新开启、充沛潜能", "延迟启动、灵感阻塞、错失先机", "带来该元素最纯粹的新生礼物与起点机会。", "能量仍在内部酝酿，尚未找到清晰出口，需避免仓促消耗。"),
      RankSpec(2, "II", "二", "Two of", "平衡选择、双极互动、初始规划", "犹豫失衡、沟通停滞、难以取舍", "代表两股力量的协调、对话与初步蓝图。", "可能出现拉扯、拖延或对立僵局，需要重整优先级。"),
      RankSpec(3, "III", "三", "Three of", "初步成果、协作扩展、成长显现", "配合失调、计划延迟、各自为政", "能量向外扩展，见证合作、远见或情感共鸣的初步开花。", "协作出现微小摩擦或预期进度放缓，宜及时校对方向。"),
      RankSpec(4, "IV", "四", "Four of", "稳固秩序、休憩整顿、阶段安定", "过度僵化、沉闷停滞、逃避突破", "筑起安全稳固的港湾，适合巩固成果与沉淀心绪。", "安稳过度可能演变为停滞，提醒你适时走出舒适界线。"),
      RankSpec(5, "V", "五", "Five of", "考验磨砺、转变张力、突破竞争", "走出阴霾、化解冲突、停止内耗", "出现必要的挑战、对比或短暂失落，淬炼出更强的韧性。", "风暴渐息，开始从挫败或分歧中抽身，看见弥补与和解的可能。"),
      RankSpec(6, "VI", "六", "Six of", "和谐回流、胜利馈赠、温柔渡河", "不平衡付出、过往执念、回报延迟", "付出迎来顺畅回响，在支持、记忆或胜利中获得舒展。", "需留意给予与接受是否失衡，或是否被往日情绪牵绊脚步。"),
      RankSpec(7, "VII", "七", "Seven of", "坚定立场、灵性探索、策略坚守", "退缩动摇、幻想过多、防卫过度", "在多重选择与考验中守住信念，或深入思索更长远的意义。", "可能因缺乏耐心或思绪繁杂而分心，建议聚焦真正重要的目标。"),
      RankSpec(8, "VIII", "八", "Eight of", "迅速推进、精进自律、突破界限", "急躁受阻、自我设限、疲惫倦怠", "能量高速运转或全神贯注打磨技艺，即将穿过瓶颈。", "节奏被打乱或陷入自我设限的焦虑中，宜放慢步伐重新梳理。"),
      RankSpec(9, "IX", "九", "Nine of", "接近圆满、独立丰盛、守望成熟", "隐忧浮现、过度防备、临门疲惫", "历经积累后的丰盈与独立光彩，距离阶段完结仅一步之遥。", "表面充足之下可能藏着担忧或孤立感，不妨允许自己放松并求助。"),
      RankSpec(10, "X", "十", "Ten of", "极致圆满、周期完结、满溢传承", "重担卸下、过度负荷、周期重启", "该元素的旅程达到顶点，迎来盛大丰收或必须整装转折的节点。", "提示不可独自背负过多责任，适度放手与更新方能轻盈前行。"),
      RankSpec(11, "XI", "侍从", "Page of", "纯净消息、好奇学习、新生信使", "稚嫩冲动、消息延迟、缺乏专注", "以开放谦逊的学习者姿态迎接新消息、新灵感与成长机会。", "想法多但落地少，或沟通表达不够成熟，需要多一份耐心打磨。"),
      RankSpec(12, "XII", "骑士", "Knight of", "全力奔赴、行动追求、使命必达", "鲁莽偏激、半途而废、停滞不前", "带着鲜明元素特质果断前行，是推动局势变化的重要行动能量。", "容易因过于急切而忽视细节，或在缺乏方向感中原地打转。"),
      RankSpec(13, "XIII", "王后", "Queen of", "内在圆满、温和掌控、深度滋养", "情绪敏感、过度操心、自信不足", "以成熟、温柔且具象的方式驾驭该元素，散发出沉静自信的魅力。", "内在能量略显失衡或过度保护自我，请先安顿自己的感受。"),
      RankSpec(14, "XIV", "国王", "King of", "大师境界、格局统御、成熟担当", "刚愎控制、滥用权威、格局受限", "代表该元素的最高成熟度，拥有远见、担当与令众人信服的稳定力量。", "可能表现为过度强势或逃避担当，提醒以宽厚与清明带领全局。"),
    )

  private fun buildSuit(
    startId: Int,
    suit: String,
    suitZh: String,
    element: String,
    astroDomain: String,
    upDomainDesc: String,
    revDomainDesc: String,
    loveFlavor: String,
    careerFlavor: String,
    spiritFlavor: String,
  ): List<TarotCardEntity> =
    ranks.mapIndexed { idx, r ->
      val cardId = startId + idx
      val fullZh =
        if (r.number <= 10) "$suitZh${r.nameZhSuffix}" else "$suitZh${r.nameZhSuffix}"
      val fullEn = "${r.nameEnPrefix} $suitZh".let {
        when (suit) {
          "WANDS" -> "${r.nameEnPrefix} Wands"
          "CUPS" -> "${r.nameEnPrefix} Cups"
          "SWORDS" -> "${r.nameEnPrefix} Swords"
          else -> "${r.nameEnPrefix} Pentacles"
        }
      }
      TarotCardEntity(
        id = cardId,
        number = r.number,
        arcana = "MINOR",
        suit = suit,
        nameZh = fullZh,
        nameEn = fullEn,
        romanNumeral = r.roman,
        element = element,
        astrology = astroDomain,
        keywordsUpright = "${r.kwUp}、${element}流动",
        keywordsReversed = "${r.kwRev}、${element}失衡",
        meaningUpright = "【$suitZh · $element】${r.themeUp}$upDomainDesc",
        meaningReversed = "【$suitZh · 逆位】${r.themeRev}$revDomainDesc",
        loveAdvice = "$loveFlavor（${r.kwUp.substringBefore("、")}）",
        careerAdvice = "$careerFlavor（${r.kwUp.substringBefore("、")}）",
        spiritualAdvice = "$spiritFlavor 记住：顺应${element}的自然节律，温柔而坚定地前进。",
      )
    }

  val minorArcana: List<TarotCardEntity> =
    buildSuit(
      startId = 22,
      suit = "WANDS",
      suitZh = "权杖",
      element = "火元素",
      astroDomain = "白羊 / 狮子 / 射手",
      upDomainDesc = "在行动力、灵感热情与自我创造领域点亮炽热而不灼人的圣火。",
      revDomainDesc = "热情可能暂时受阻或过度焦躁，宜梳理真正值得投入的方向。",
      loveFlavor = "关系中充满热情吸引与积极表达，勇敢迈出温暖的一步",
      careerFlavor = "创意项目与行动执行力提升，适合主动争取与开拓新版图",
      spiritFlavor = "唤醒内在生命火焰与自信勇气，化愿景为真实行动。",
    ) +
      buildSuit(
        startId = 36,
        suit = "CUPS",
        suitZh = "圣杯",
        element = "水元素",
        astroDomain = "巨蟹 / 天蝎 / 双鱼",
        upDomainDesc = "在情感联结、温柔直觉与内心疗愈层面倾注澄澈圣水。",
        revDomainDesc = "情绪容易敏感起伏或有所保留，请优先倾听并安抚自己。",
        loveFlavor = "心意流动细腻深情，真诚分享感受能让彼此更加贴近",
        careerFlavor = "注重团队氛围与直觉灵感，艺术、咨询与人际协作尤佳",
        spiritFlavor = "让情感如圣泉般澄净流动，以慈悲温柔拥抱此刻。",
      ) +
      buildSuit(
        startId = 50,
        suit = "SWORDS",
        suitZh = "宝剑",
        element = "风元素",
        astroDomain = "双子 / 天秤 / 水瓶",
        upDomainDesc = "在思维清明、客观沟通与决断边界上展现如风般的洞察力。",
        revDomainDesc = "思绪可能过度紧绷或陷入言辞误会，切忌让理性化作自我苛责。",
        loveFlavor = "需要坦诚而温柔的沟通，厘清误解才能建立真正信任",
        careerFlavor = "适合策略分析、方案梳理与果断决策，保持事实清晰",
        spiritFlavor = "用清明如镜的觉知斩断无谓纷扰，守住内心澄明。",
      ) +
      buildSuit(
        startId = 64,
        suit = "PENTACLES",
        suitZh = "星币",
        element = "土元素",
        astroDomain = "金牛 / 处女 / 摩羯",
        upDomainDesc = "在现实耕耘、财务丰盛与身心安稳层面结出踏实可靠的果实。",
        revDomainDesc = "可能对物质成果感到焦虑或过度透支精力，请回归踏实节奏。",
        loveFlavor = "以真诚陪伴、实际照顾与长久承诺浇灌安稳踏实的爱意",
        careerFlavor = "稳步累积专业价值与实质回报，重视细节、信用与长期复利",
        spiritFlavor = "将神圣智慧落实于一餐一饭与日常耕耘之中。",
      )
}
