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

                    LazyVStack {

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
                                            VStack {
                                                Text(day.title)
                                            }
                                                    .frame(minWidth: 0, maxWidth: .infinity)
                                        } else {
                                            ZStack {
                                            }
                                                    .frame(minWidth: 0, maxWidth: .infinity)
                                        }
                                    }
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
