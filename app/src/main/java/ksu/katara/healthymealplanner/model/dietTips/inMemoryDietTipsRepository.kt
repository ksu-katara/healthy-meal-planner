package ksu.katara.healthymealplanner.model.dietTips

import ksu.katara.healthymealplanner.exceptions.DietTipNotFoundException
import ksu.katara.healthymealplanner.model.dietTips.entities.DietTip
import ksu.katara.healthymealplanner.model.dietTips.entities.DietTipDetails
import ksu.katara.healthymealplanner.tasks.SimpleTask
import ksu.katara.healthymealplanner.tasks.Task
import java.util.concurrent.Callable

typealias DietTipsListener = (dietTips: List<DietTip>) -> Unit

class InMemoryDietTipsRepository : DietTipsRepository {

    private var dietTips = mutableListOf<DietTip>()
    private var loaded = false

    private val listeners = mutableSetOf<DietTipsListener>()

    override fun loadDietTipsForHomeScreen(): Task<Unit> = SimpleTask {
        Thread.sleep(2000)

        dietTips = (0 until DIET_TIPS_AMOUNT_FOR_HOME_SCREEN).map {
            DietTip(
                id = it.toLong(),
                name = DIET_TIP_NAMES[it],
                photo = DIET_TIP_IMAGES[it]
            )
        }.toMutableList()

        dietTips.add(
            DietTip(
                id = DIET_TIPS_AMOUNT_FOR_HOME_SCREEN.toLong(),
                name = "Далее",
                photo = "https://images.unsplash.com/photo-1639763832833-242273ded085?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=2370&q=80",
            )
        )

        loaded = true
        notifyChanges()
    }

    override fun getById(id: Long): Task<DietTipDetails> = SimpleTask(Callable {
        Thread.sleep(2000)
        val dietTip = dietTips.firstOrNull { it.id == id } ?: throw DietTipNotFoundException()
        return@Callable DietTipDetails(
            dietTip = dietTip,
            DIET_TIP_DETAILS_BACKGROUND[id.toInt()],
            DIET_TIP_DETAILS_TITLES.getValue(DIET_TIP_NAMES[id.toInt()])[0],
            DIET_TIP_DETAILS_DESCRIPTIONS.getValue(DIET_TIP_NAMES[id.toInt()])[0],
        )
    })

    override fun addListener(listener: DietTipsListener) {
        listeners.add(listener)
        if (loaded) {
            listener.invoke(dietTips)
        }
    }

    override fun removeListener(listener: DietTipsListener) {
        listeners.remove(listener)
    }

    private fun notifyChanges() {
        if (!loaded) return
        listeners.forEach { it.invoke(dietTips) }
    }

    companion object {
        private val DIET_TIPS_AMOUNT_FOR_HOME_SCREEN = 5

        private val DIET_TIP_IMAGES = mutableListOf(
            "https://images.unsplash.com/photo-1490645935967-10de6ba17061?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=2353&q=80",
            "https://images.unsplash.com/photo-1614887065001-06c958a7cddd?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=987&q=80",
            "https://images.unsplash.com/photo-1656218257936-8384471a0258?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=2274&q=80",
            "https://images.unsplash.com/photo-1518611012118-696072aa579a?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=2370&q=80",
            "https://images.unsplash.com/photo-1545389336-cf090694435e?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1364&q=80",
        )

        private val DIET_TIP_NAMES = mutableListOf(
            "Режим питания",
            "Вода",
            "Сон",
            "Спорт",
            "Медитация",
        )

        private val DIET_TIP_DETAILS_BACKGROUND = mutableListOf(
            "https://images.unsplash.com/photo-1494859802809-d069c3b71a8a?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=2370&q=80",
            "https://images.unsplash.com/photo-1527904188605-3424bcc2d107?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=2050&q=80",
            "https://images.unsplash.com/photo-1519750783826-e2420f4d687f?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=987&q=80",
            "https://images.unsplash.com/photo-1516352267226-f5f3e4c53781?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=966&q=80",
            "https://images.unsplash.com/photo-1585639408964-b8938d59264e?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=2370&q=80",
        )

        private val DIET_TIP_DETAILS_TITLES = mutableMapOf(
            "Режим питания" to mutableListOf(
                "Подготовка к приему пищи",
                "Приемущества тщательного пережевывания",
                "Во время приема пищи",
                "Дробное питание: за и против?",
                "Базовые принципы здорового питания",
                "Формируем корзину из цельных продуктов",
                "Планируем рацион",
                "Как облегчить приготовление пищи",
            ),
            "Вода" to mutableListOf(
                "Признаки того, что вы пьете мало воды",
                "Характеристики воды",
                "pH воды",
                "Плюсы воды с отрицательным ОВП",
                "Средства для изменения качества воды",
            ),
            "Сон" to mutableListOf(
                "Сон - лучшее лекарство",
                "Мелатонин - anti-age гормон",
                "Рекомендации при проблемах со сном",
            ),
            "Спорт" to mutableListOf(
                "Рекомендации",
                "Дыхательные практики",
            ),
            "Медитация" to mutableListOf(
                "Рекомендации",
                "Виды медитаций",
            ),
        )

        private val DIET_TIP_DETAILS_DESCRIPTIONS = mutableMapOf(
            "Режим питания" to mutableListOf(
                "- Пищеварение начинается с мысли о еде\n" +
                        "- Едим по голоду, не заедаем стресс\n" +
                        "- Внешний вид еды играет важную роль\n" +
                        "- Не есть на ходу, стоя\n" +
                        "- Есть за столом в спокойном состоянии, не испытывая стресс" +
                        "- Не использовать гаджены во время приема пищи\n" +
                        "- Для лучшего контроля тщытельного пережевывания:" +
                        "откусите кусочек, положите вилку на стол. Тщательно пережуйте." +
                        "Возьмите вилку и положите следующую порцию еды в рот",
                "- В процессе тщательного пережевывания выделяется слюна, в которой содержатся" +
                        "ферментыб помогающие расщеплять углеводы(особенно важно тем, у кого" +
                        "есть проблемы с поджелудочной железой)\n" +
                        "- В слюне содержатся антибактериальные вещества, помогающие боротся" +
                        "с вирусами и бактериями\n" +
                        "- Чем больше выделяется слюны, тем больше выделяется соляной кислоты и " +
                        "желчи для лучшего пищеварения\n" +
                        "- Происходит более быстрое чувство насыщения",
                "- Во время еды не пьем никакие напитки, кроме теплой воды(не более 100-150 мл," +
                        " 38-40 градусов)\n" +
                        "- В каждый прием пищи включаем овощи и зелень",
                "- Дробное питание может приводить к заболеваниям\n" +
                        "- В зависимости от состояния ЖКТ и сопутствующих заболевания, фрукты необходимо " +
                        "употреблять либо за 15-20 минут до еды, либо после еды\n" +
                        "- Промышленные сладости и сахар исключаются\n" +
                        "- Любые десерты едим после основного приема пищи, не на перекус\n" +
                        "- Можно между приемами пищи оставить чай, травяной чай, цикорий, какао, кофе, если " +
                        "разрешены эти напитки на лечебном протоколе. Этот напиток не должен быть с каким-то " +
                        "десертом, иначе это уже будет прием пищи\n",
                "- Исключить добавленный сахар\n" +
                        "- Читать составы\n" +
                        "- Минимизировать/исключить обработанные проодукты(фаст-фуд, полуфабрикаты, пельмени, " +
                        "колбасы, сосиски и другое)\n" +
                        "- Использовать цельные продукты\n" +
                        "- Не жарить блюда до коричневой корочки(лучше тушить, готовить на пару, запекать)\n" +
                        "- Убрать все продукты воспаления из дома, чтобы не было соблазнов\n" +
                        "- Выбирать простые блюда на первом этапе\n" +
                        "- Делать упор на разнообразие рациона, а не на сложность блюда\n",
                "- Мясо, субпродукты, птица, рыба, морепродукты, яйца\n" +
                        "- Овощи, зелень, ягоды, фрукты\n" +
                        "- Бобовые: нут, маш, чечевица\n" +
                        "- Злаки: гречка, рис, амарант, кеноа\n" +
                        "- Нерафинированные масла, семена, орехи\n",
                "- Яйца могут быть через день\n" +
                        "- Красное мясо 2 раза в неделю\n" +
                        "- Птица 3 раза в неделю\n" +
                        "- Рыба и морепродукты 2 раза в неделю(можно чаще)\n" +
                        "- Обязательно включайте в рацион субпродукты(печень, сердечки и другое)\n" +
                        "- Каджый день включайте разные злаки и бобовые(если разрешены на лечебном протоколе)\n" +
                        "- Каждый день новый овощ, зелень: различные виды капусты(белокочанная, цветная, " +
                        "брюссельская, кольраби), свекла, морковь и другое\n",
                "- Заранее сварить крупу и хранить в стеклянном контейнере в холодильнике\n" +
                        "- Купить консервы без сахара и масла(сардины, тунец, скумбрия, кукуруза, горох)\n" +
                        "- Купить замороженные овощи\n" +
                        "- Сварить заранее яйца\n" +
                        "- Заранее приготовить мясо, субпродукты, паштеты и другое\n" +
                        "- Заморозить костные и овощниые бульоны\n" +
                        "- Пользоваться мультиваркой, пароваркой\n",
            ),
            "Вода" to mutableListOf(
                "- Жажда и сухость во рту\n" +
                        "- Моча темно-желтого цвета\n" +
                        "- Нерегулярный стул\n" +
                        "- Сухая кожа, поменялся цвет лица\n" +
                        "- Боли в суставах\n" +
                        "- Образование целлюлита\n" +
                        "- Неприятный запах пота\n" +
                        "! Норма воды в день: 30 мл на 1 кг идеального веса\n",
                "- Температура воды:\n" +
                        "Пейте теплую воду(38-40 градусов) или хотя бы комнатной температуры." +
                        "Начинайти пить постепенно, небольшими глоточками\n" +
                        "- Ph воды:\n" +
                        "В среднем pH организма варируется от 7-7.5, поэтому вода не должна" +
                        "быть ниже 7.5(а лучше 7.5-8.5). Воду с pH 9-10 не пьем на постоянной основе, " +
                        "она показана только во время болезни, при острых инфекционных заболеваниях\n" +
                        "- Окислительно-восстановительный потенциал(ОВП)\n" +
                        "ОВП - это активность электронов, участвующих в окислительно-восстановительной " +
                        "реакции. ОВП организма 100-200милливольт, а питьевая вода имеет ОВП от " +
                        "+200 до +300 милливольт. Чтобы организм не тратил энергию на изменение ОВП, " +
                        "нужно использовать качественную воду\n",
                "Для определния pH воды можно использовать pH-полоски и pH-метры. Для измерения pH характиристик" +
                        "можно брать кипяченную, фильтрованную, диистиллированную воду",
                "- Стабилизация уровня глюкозы в крови\n" +
                        "- Восстановление эластичности сосудов\n" +
                        "- Восстановление давления\n" +
                        "- Выводит свободные радикалы из организма\n" +
                        "- Стимулирует функции пищеварительной системы\n" +
                        "- Улучшает обмен веществ\n",
                "- Эфирные масла цитрусовых(1 капля на стакан воды) - " +
                        "лимон, грейпфрут, апельсин, мандарие и другое\n" +
                        "- Минеральные концентраты\n" +
                        "- Электролитическая минеральная добавка Coral mine\n" +
                        "- Водородные бутылки, кувшины\n" +
                        "- Ионизаторы\n",
            ),
            "Сон" to mutableListOf(
                "Совы - это не выспавшиеся жаворонки.\n" +
                        "Мелатонин - гормон сна и его выработка начинается в 19 часов и " +
                        "достигает пика к 23. Восстановление организма происходит с 23 до 1 ночи, " +
                        "поэтому важно спать в это время!\n" +
                        "Готовимся ко сну с 22, в 23 уже спим",
                "- Обладает антивозрастным эффектом\n" +
                        "- Улучшает иммунитет\n" +
                        "- Восстанавливает уровень лептина(гормона насыщения)\n" +
                        "- Улучшает работу клеток головного мозга\n" +
                        "- Предотвращает развитие опухолей\n",
                "- Обязательно завтракаем\n" +
                        "- Не едим за 3-4 часа до сна(не позднее 7 часов вечера)\n" +
                        "- Если есть проблемы с храническим стрессом, можно сделать легкий " +
                        "перекус без углеводов за 1 час до сна\n" +
                        "- Прием магниевых ванн за 40 минут до сна(вода не выше 40 градусов)\n" +
                        "- Температура в комнате в районе 21 градуса\n" +
                        "- Беруши\n" +
                        "- Маска для сна\n" +
                        "- Плотные шторы\n" +
                        "- Ставим ночной режим на гаджеты\n" +
                        "- Специальные очки для изменения спектра света\n" +
                        "- Медитации\n",
            ),
            "Спорт" to mutableListOf(
                "- Проходите в день минимум 10000 шагов\n" +
                        "- Подбирайте физическую активность по силам и по самочуствию\n" +
                        "- Спорт влияет на работу блуждающего нерва\n" +
                        "- Тонус мышц тазового дна зависит от диафрагмального дыхания\n" +
                        "- Нет правил по приему пищи при занятиях спортом\n",
                "- Стрельниковой\n" +
                        "- Бутейко\n" +
                        "- Пранаяма\n",
            ),
            "Медитация" to mutableListOf(
                "- Выберите удобное время\n" +
                        "- Выберите тихое место\n" +
                        "- Сядьте удобно\n" +
                        "- Сохраняйте желудок полупустым\n" +
                        "- Начните с разминки\n" +
                        "- Сделайте несколько глубоких вдохов и выдохов\n" +
                        "- Сохраняйте на лице мягкую улыбку\n" +
                        "- Открывайте глаза медленно и постепенно\n",
                "- Медитация Випассана\n" +
                        "- Медитация Шаматха\n" +
                        "- Трансцендентальная медитация\n" +
                        "- Медитация на чакры\n" +
                        "- Медитация в йоге\n" +
                        "- Дзен-медитация\n",
            ),
        )
    }
}