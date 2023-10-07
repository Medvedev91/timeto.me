import SwiftUI
import shared

struct DatePickerStateView: View {

    @State private var dateRange: ClosedRange<Date>
    private let minTimeState: UnixTime
    private let maxTimeState: UnixTime

    @State private var formDate: Date
    private let timeState: UnixTime
    private let onDayChanged: (UnixTime) -> Void

    init(
            unixTime: UnixTime,
            minTime: UnixTime,
            maxTime: UnixTime,
            onDayChanged: @escaping (UnixTime) -> Void
    ) {
        _formDate = State(initialValue: unixTime.toDate())
        timeState = unixTime
        self.onDayChanged = onDayChanged

        _dateRange = State(initialValue: minTime.toDate()...maxTime.toDate())
        minTimeState = minTime
        maxTimeState = maxTime
    }

    var body: some View {
        DatePicker(
                "",
                selection: $formDate,
                in: dateRange,
                displayedComponents: [.date]
        )
                //////
                .onChange(of: formDate) { newDate in
                    let newTime = newDate.toUnixTime()
                    if (newTime.time != timeState.time) {
                        onDayChanged(newTime)
                    }
                }
                .onChange(of: timeState) { newTime in
                    formDate = newTime.toDate()
                }
                //////
                .onChange(of: minTimeState) { newTime in
                    dateRange = newTime.toDate()...maxTimeState.toDate()
                }
                .onChange(of: maxTimeState) { newTime in
                    dateRange = minTimeState.toDate()...newTime.toDate()
                }
    }
}
