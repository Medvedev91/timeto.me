import SwiftUI
import shared

struct EventsCalendarDayView: View {

    @State private var vm: EventsCalendarDayVM

    init(
        unixDay: Int
    ) {
        _vm = State(initialValue: EventsCalendarDayVM(unixDay: unixDay.toInt32()))
    }

    var body: some View {

        VMView(vm: vm) { state in

            Text("eee")
        }
    }
}
