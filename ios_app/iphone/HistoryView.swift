import SwiftUI
import shared

///
/// A lot of trick to auto-scroll to down on screen opens
///
struct HistoryView: View {

    @State private var vm = HistoryVm()

    @Binding var isHistoryPresented: Bool

    @State private var isSelectDateToMovePresented = false
    @State private var selectedDateToMove = Date()

    @State private var isEditMode = false

    var body: some View {

        VMView(vm: vm, stack: .ZStack(alignment: .bottom)) { state in

            ScrollViewReader { scrollView in

                ScrollView(.vertical, showsIndicators: false) {

                    LazyVStack(pinnedViews: [.sectionFooters]) {

                        // Because the list is upside down it is on the bottom
                        ZStack {}
                                .id("history_bottom_item")
                                .fixedSize(horizontal: false, vertical: true)
                                .frame(height: 70)

                        ForEach(state.sections.reversed(), id: \.self.day) { (section: HistoryVm.HistorySection) in

                            Section(
                                    footer:
                                    Button(
                                            action: {
                                                isSelectDateToMovePresented = true
                                            },
                                            label: {
                                                Text(section.dayText)
                                                        .padding(.horizontal, 9)
                                                        .padding(.vertical, 5)
                                                        .background(
                                                                ZStack {
                                                                    Capsule().fill(c.bg.opacity(0.5))
                                                                    Capsule()
                                                                            .fill(.blue)
                                                                            .padding(1 / UIScreen.main.scale)
                                                                }
                                                        )
                                                        .foregroundColor(.white)
                                                        // Обязательно, иначе плохо работает скролл вниз
                                                        .frame(height: 50)
                                                        .fixedSize()
                                                        ////
                                                        .font(.system(size: 14))
                                            }
                                    )
                                            .id("day_" + section.day.toString())
                                            .flippedUpsideDown()
                            ) {
                                ForEach(section.intervals.reversed(), id: \.id) { (interval: IntervalDb) in

                                    let intervalUI = HistoryVm.IntervalUI.companion.build(interval: interval, section: section)

                                    HStack(alignment: .top, spacing: 10) {

                                        HStack(alignment: .center, spacing: 0) {

                                            if !intervalUI.isStartsPrevDay {

                                                if isEditMode {
                                                    HStack(spacing: 10) {
                                                        Button(
                                                                action: {
                                                                    intervalUI.delete()
                                                                },
                                                                label: {
                                                                    Image(systemName: "minus.circle.fill")
                                                                            .foregroundColor(.red)
                                                                }
                                                        )
                                                        EditIntervalButtonView(
                                                                historyView: self,
                                                                intervalUI: intervalUI
                                                        )
                                                    }
                                                            .padding(.top, 1)
                                                } else {
                                                    Text(intervalUI.periodString)
                                                            .foregroundColor(.primary)
                                                            .font(.system(size: 12, weight: .thin))
                                                            .frame(alignment: .leading)
                                                }

                                                Spacer()

                                                Text(intervalUI.timeString)
                                                        .foregroundColor(.primary)
                                                        .font(.system(size: 15, weight: .medium, design: .monospaced))
                                                        .frame(alignment: .trailing)
                                            }
                                        }
                                                .frame(width: 100, alignment: .leading)
                                                .padding(.top, 3)

                                        VStack {

                                            RoundedRectangle(cornerRadius: 20, style: .continuous)
                                                    .fill(interval.getActivityDbCached().colorRgba.toColor())
                                                    .frame(
                                                            width: 10,
                                                            height: Double(10.limitMin(intervalUI.secondsForBar.toInt() / 50))
                                                    )
                                                    .padding(.leading, 5)
                                                    .padding(.trailing, 5)

                                            if isEditMode {
                                                AddButtonView(
                                                        // To not to always the the error that time is not available
                                                        selectedDate: intervalUI.barTimeFinish.asTimeToDate().inSeconds(-1),
                                                        state: state
                                                )
                                                        .padding(.top, 5) // Для симметрии
                                            }
                                        }
                                                .frame(maxHeight: .infinity)

                                        VStack(spacing: 4) {

                                            if !intervalUI.isStartsPrevDay {

                                                Text(intervalUI.text)
                                                        .foregroundColor(.primary)
                                                        .font(.system(size: 16, weight: .medium))
                                                        .frame(maxWidth: .infinity, alignment: .leading)
                                            }
                                        }
                                                .frame(maxWidth: .infinity)
                                                .padding(.top, 3)
                                    }
                                            .padding(.top, section.intervals.first == interval ? 6 : 0)
                                            .padding(.leading, 20)
                                            .padding(.trailing, 20)
                                            .listRowSeparator(.hidden)
                                            .fixedSize(horizontal: false, vertical: true)
                                            // To unique. It is possible the same intervals in different days.
                                            .id("h_interval_\(section.day)_\(interval.id)")
                                            .flippedUpsideDown()
                                }
                            }
                        }
                    }
                }
                        .onChange(of: selectedDateToMove) { newSelectedDateToMove in
                            isSelectDateToMovePresented = false
                            let dayToMove = vm.calcDayToMove(selectedDay: selectedDateToMove.toUnixTime().localDay)
                            for i in 0...4 {
                                let delay = 0.3 * i.toDouble()
                                myAsyncAfter(delay) {
                                    withAnimation {
                                        scrollView.scrollTo("day_\(dayToMove)", anchor: .bottom)
                                    }
                                }
                            }
                        }
                        .flippedUpsideDown()
            }
                    .ignoresSafeArea()

            HStack(alignment: .center) {

                Button(
                        action: {
                            withAnimation {
                                isEditMode.toggle()
                            }
                        },
                        label: {
                            Image(systemName: "pencil")
                                    .foregroundColor(isEditMode ? .white : .secondary)
                                    .scaleEffect(1.1)
                        }
                )
                        .frame(width: 35, height: 35)
                        .background(roundedShape.fill(isEditMode ? .blue : Color(.systemBackground)))

                Spacer()

                DialogCloseButton(isPresented: $isHistoryPresented, trailing: 0, bottom: 0, withSaveArea: false)
            }
                    .padding(.bottom, 8)
                    .padding(.horizontal, DialogCloseButton.DEF_TRAILING)
                    .sheetEnv(isPresented: $isSelectDateToMovePresented) {
                        SelectDateToMoveSheet(
                                selectedDate: $selectedDateToMove,
                                minPickerDate: state.minPickerDay.asUnixDayToDate()
                        )
                    }
        }
                .background(Color(.systemBackground))
    }

    private struct EditIntervalButtonView: View {

        let historyView: HistoryView
        var intervalUI: HistoryVm.IntervalUI
        @State private var isSheetPresented = false

        var body: some View {

            Button(
                    action: {
                        withAnimation {
                            isSheetPresented = true
                        }
                    },
                    label: {
                        Image(systemName: "pencil")
                                .foregroundColor(.blue)
                    }
            )
                    .sheetEnv(isPresented: $isSheetPresented) {
                        SheetForm(
                                isPresented: $isSheetPresented,
                                intervalUI: intervalUI
                        )
                    }
        }

        private struct SheetForm: View {

            @Binding private var isPresented: Bool
            @State private var selectedDate: Date
            private let intervalUI: HistoryVm.IntervalUI

            init(
                    isPresented: Binding<Bool>,
                    intervalUI: HistoryVm.IntervalUI
            ) {
                _isPresented = isPresented
                _selectedDate = State(initialValue: intervalUI.interval.unixTime().toDate())
                self.intervalUI = intervalUI
            }

            var body: some View {

                NavigationView {

                    VStack {

                        Form {

                            Section {

                                DatePicker(
                                        "",
                                        selection: $selectedDate,
                                        in: ...Date(),
                                        displayedComponents: [.date, .hourAndMinute]
                                )
                                        .labelsHidden()
                                        .datePickerStyle(.wheel)
                            }

                            Section {

                                HStack {
                                    Spacer()
                                    Button("Save") {
                                        intervalUI.upTime(newTime: selectedDate.toUnixTime())
                                    }
                                    Spacer()
                                }
                            }
                        }
                    }
                            .ignoresSafeArea(edges: .bottom)
                }
            }
        }
    }

    private struct AddButtonView: View {

        @State var selectedDate: Date
        let state: HistoryVm.State

        @State private var isSheetPresented = false

        var body: some View {
            Button(
                    action: {
                        withAnimation {
                            isSheetPresented.toggle()
                        }
                    },
                    label: {
                        Image(systemName: "plus")
                                .resizable()
                                .frame(width: 11, height: 11)
                                .foregroundColor(.white)
                    }
            )
                    .frame(width: 22, height: 22)
                    .background(roundedShape.fill(.blue))
                    .sheetEnv(isPresented: $isSheetPresented) {
                        SheetForm(
                                isPresented: $isSheetPresented,
                                selectedDate: selectedDate,
                                state: state
                        )
                    }
        }

        private struct SheetForm: View {

            @Binding private var isPresented: Bool
            @State private var selectedDate: Date
            private let state: HistoryVm.State

            @State private var selectedActivityId: Int32

            init(
                    isPresented: Binding<Bool>,
                    selectedDate: Date,
                    state: HistoryVm.State
            ) {
                self.state = state
                _isPresented = isPresented
                _selectedDate = State(initialValue: selectedDate)
                _selectedActivityId = State(initialValue: state.activitiesFormAddUI.first!.activity.id)
            }

            var body: some View {

                NavigationView {

                    VStack {

                        Form {

                            Section {

                                Picker("Activity", selection: $selectedActivityId) {
                                    ForEach(state.activitiesFormAddUI, id: \.activity.id) { activityUI in
                                        Text(activityUI.activity.nameWithEmoji(isLeading: false))
                                    }
                                }
                                        .foregroundColor(.primary)
                            }

                            Section {

                                DatePicker(
                                        "",
                                        selection: $selectedDate,
                                        in: ...Date(),
                                        displayedComponents: [.date, .hourAndMinute]
                                )
                                        .labelsHidden()
                                        .datePickerStyle(.wheel)
                            }

                            Section {

                                HStack {
                                    Spacer()
                                    Button("Save") {
                                        let activityUI = state.activitiesFormAddUI.first { $0.activity.id == selectedActivityId }!
                                        activityUI.addInterval(unixTime: selectedDate.toUnixTime()) {
                                            isPresented = false
                                        }
                                    }
                                    Spacer()
                                }
                            }
                        }
                    }
                            .ignoresSafeArea(edges: .bottom)
                }
            }
        }
    }

    private struct SelectDateToMoveSheet: View {

        @Binding var selectedDate: Date
        let minPickerDate: Date

        var body: some View {

            DatePicker(
                    "Start Date",
                    selection: $selectedDate,
                    in: (minPickerDate...Date()),
                    displayedComponents: [.date]
            )
                    .labelsHidden()
                    .padding(.horizontal, 30)
                    .padding(.top, 20)
                    .datePickerStyle(.graphical)
        }
    }
}
