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
            }
                    .padding(.horizontal, H_PADDING)
        }
    }
}
