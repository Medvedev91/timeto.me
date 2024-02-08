import SwiftUI
import shared

struct EventsCalendarView: View {

    @State private var vm = EventsCalendarVM()

    var body: some View {

        VMView(vm: vm) { state in

            VStack {

                VStack {

                    HStack {

                        ForEachIndexed(state.weekTitles) { _, weekTitle in

                            HStack {
                                Spacer()
                                Text(weekTitle.title)
                                        .font(.system(size: 11))
                                        .foregroundColor(weekTitle.isBusiness ? c.text : c.textSecondary)
                                Spacer()
                            }
                        }
                    }

                    DividerBg()
                            .padding(.top, 2.0)
                }

                ScrollView(showsIndicators: false) {

                    LazyVStack(spacing: 0) {

                        ForEachIndexed(state.months) { _, month in

                            HStack {

                                ForEach(0..<month.emptyStartDaysCount, id: \.self) { _ in
                                    ZStack {
                                    }
                                            .frame(minWidth: 0, maxWidth: .infinity)
                                }

                                Text(month.title)
                                        .frame(minWidth: 0, maxWidth: .infinity)
                                        .foregroundColor(c.white)
                                        .padding(.top, 16)
                                        .padding(.bottom, 8)
                                        .font(.system(size: 17, weight: .bold))

                                ForEach(0..<month.emptyEndDaysCount, id: \.self) { _ in
                                    ZStack {
                                    }
                                            .frame(minWidth: 0, maxWidth: .infinity)
                                }
                            }

                            let weeks = month.weeks as! [[EventsCalendarVM.MonthDay?]]

                            // Fix nested ForEachIndexed()
                            ForEach(weeks, id: \.self) { week in

                                HStack {

                                    ForEachIndexed(week) { _, day in

                                        if let day = day {

                                            let bgColor: Color = {
                                                let selectedDay = state.selectedDay?.toInt() ?? -1
                                                if day.unixDay == selectedDay {
                                                    return c.blue
                                                } else if day.isToday {
                                                    return c.purple
                                                }
                                                return c.transparent
                                            }()

                                            Button(
                                                action: {
                                                    vm.setSelectedDay(unixDay: day.unixDay)
                                                },
                                                label: {

                                                    VStack {

                                                        DividerBg()

                                                        Text(day.title)
                                                                .foregroundColor(day.isBusiness ? c.white : c.textSecondary)
                                                                .padding(.top, 6)

                                                        ForEach(day.previews, id: \.self) { preview in

                                                            Text(preview)
                                                                    .padding(.horizontal, 1)
                                                                    .foregroundColor(c.textSecondary)
                                                                    .font(.system(size: 11, weight: .light))
                                                                    .lineLimit(1)
                                                        }
                                                    }
                                                            .padding(.bottom, 2)
                                                            .frame(minWidth: 0, maxWidth: .infinity)
                                                }
                                            )
                                                    .background(bgColor)
                                        } else {
                                            ZStack {
                                            }
                                                    .frame(minWidth: 0, maxWidth: .infinity)
                                        }
                                    }
                                }

                                let selectedDay = week.first { item in
                                    if let item = item, let selectedDay = state.selectedDay {
                                        return item.unixDay.toInt() == selectedDay.toInt()
                                    }
                                    return false
                                }

                                let isDaySelected = selectedDay != nil
                                if isDaySelected {
                                    EventsCalendarDayView(unixDay: selectedDay!!.unixDay.toInt())
                                }
                            }
                        }
                    }
                }
            }
                    .padding(.horizontal, H_PADDING)
        }
    }
}
