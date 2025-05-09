import SwiftUI
import shared

struct RepeatingFormPeriodSheet: View {
    
    let initPeriod: RepeatingDbPeriod?
    let onDone: (RepeatingDbPeriod) -> Void
    
    var body: some View {
        VmView({
            RepeatingFormPeriodVm(
                initPeriod: initPeriod
            )
        }) { vm, state in
            RepeatingFormPeriodSheetInner(
                vm: vm,
                state: state,
                activePeriodIdx: state.activePeriodIdx,
                selectedNDays: state.selectedNDays,
                onDone: onDone
            )
        }
    }
}

///

private let dayOfMonthItemViewItemSize = 36.0

private struct RepeatingFormPeriodSheetInner: View {
    
    let vm: RepeatingFormPeriodVm
    let state: RepeatingFormPeriodVm.State
    
    @State var activePeriodIdx: Int32
    @State var selectedNDays: Int32
    
    let onDone: (RepeatingDbPeriod) -> Void
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation
    
    @State private var editMode: EditMode = .active

    var body: some View {
        List {
            
            Section {
                
                Picker("Type", selection: $activePeriodIdx) {
                    ForEach(state.periodPickerItemsUi, id: \.idx) { itemUi in
                        Text(itemUi.title)
                    }
                }
                .foregroundColor(.primary)
                .onChange(of: activePeriodIdx) { _, newActivePeriodIdx in
                    vm.setActivePeriodIdx(newIdx: newActivePeriodIdx)
                }
            }
            
            if state.activePeriodIdx == 1 {
                Section {
                    Picker("", selection: $selectedNDays) {
                        ForEach(2..<667, id: \.self) {
                            Text("\($0)").tag($0.toInt32())
                        }
                    }
                    .pickerStyle(.wheel)
                    .onChange(of: selectedNDays) { _, newValue in
                        vm.setSelectedNDays(newNDays: newValue.toInt32())
                    }
                }
            }
            else if state.activePeriodIdx == 2 {
                Section {
                    ForEach(state.daysOfWeekUi, id: \.idx) { dayOfWeekUi in
                        Button(
                            action: {
                                vm.toggleDayOfWeek(dayOfWeekIdx: dayOfWeekUi.idx)
                            },
                            label: {
                                HStack {
                                    Text(dayOfWeekUi.title)
                                        .foregroundColor(.primary)
                                    Spacer()
                                    if state.selectedDaysOfWeek.toSwift().contains(dayOfWeekUi.idx.toInt()) {
                                        Image(systemName: "checkmark")
                                            .foregroundColor(.blue)
                                    }
                                }
                            }
                        )
                    }
                }
            }
            else if state.activePeriodIdx == 3 {
                let dayNumbers: [Int] = Array(1..<(RepeatingDb.companion.MAX_DAY_OF_MONTH.toInt() + 1))
                VStack(alignment: .leading, spacing: 12) {
                    ForEach(dayNumbers.chunked(7), id: \.self) { chunk in
                        HStack {
                            ForEach(chunk, id: \.self) { day in
                                let isDaySelected: Bool = state.selectedDaysOfMonth.contains(day.toKotlinInt())
                                DayOfMonthItemView(
                                    text: "\(day)",
                                    isSelected: isDaySelected,
                                    onClick: {
                                        vm.toggleDayOfMonth(dayOfMonth: day.toInt32())
                                    }
                                )
                                if day % 7 != 0 {
                                    Spacer()
                                }
                            }
                            ForEach((0..<(7-chunk.count)), id: \.self) { day in
                                Text("")
                                    .frame(width: dayOfMonthItemViewItemSize)
                            }
                        }
                    }
                    let isDaySelected: Bool = state.selectedDaysOfMonth.contains(RepeatingDb.companion.LAST_DAY_OF_MONTH.toInt().toKotlinInt())
                    DayOfMonthItemView(
                        text: "Last Day of the Month",
                        isSelected: isDaySelected,
                        hPaddings: 12,
                        onClick: {
                            vm.toggleDayOfMonth(dayOfMonth: RepeatingDb.companion.LAST_DAY_OF_MONTH)
                        }
                    )
                    .padding(.top, 2)
                }
                .customListItem()
                .frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
                .padding(.top, 16)
                .padding(.bottom, 16)
                .padding(.horizontal, H_PADDING)
            }
            else if state.activePeriodIdx == 4 {
                Section {
                    ForEach(state.selectedDaysOfYear, id: \.self) { dayOfYear in
                        Text(dayOfYear.getTitle(isShortOrLong: false))
                    }
                    .onDelete { indexSet in
                        for idx in indexSet {
                            vm.deleteDayOfTheYear(idx: idx.toInt32())
                        }
                    }
                    Button("New Day") {
                        navigation.sheet {
                            DayOfTheYearFormSheet(
                                onDone: { newDay in
                                    vm.addDayOfTheYear(item: newDay)
                                }
                            )
                        }
                    }
                }
            }
        }
        .myFormContentMargins()
        .interactiveDismissDisabled()
        .environment(\.editMode, $editMode)
        .navigationTitle(state.title)
        .toolbarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .cancellationAction) {
                Button("Cancel") {
                    dismiss()
                }
            }
            ToolbarItem(placement: .primaryAction) {
                Button("Done") {
                    vm.buildSelectedPeriod(
                        dialogsManager: navigation,
                        onSuccess: { period in
                            onDone(period)
                            dismiss()
                        }
                    )
                }
                .fontWeight(.semibold)
            }
        }
    }
}

private struct DayOfMonthItemView: View {
    
    let text: String
    let isSelected: Bool
    var hPaddings: CGFloat = 0
    let onClick: () -> Void
    
    var body: some View {
        Button(
            action: {
                onClick()
            },
            label: {
                Text(text)
                    .foregroundColor(isSelected ? .white : .primary)
                    .frame(minWidth: dayOfMonthItemViewItemSize, minHeight: dayOfMonthItemViewItemSize)
                    .font(.system(size: 14, weight: .semibold))
                    .padding(.horizontal, hPaddings)
                    .background(roundedShape.fill(isSelected ? AnyShapeStyle(.blue) : AnyShapeStyle(.quaternary)))
            }
        )
    }
}

private struct DayOfTheYearFormSheet: View {
    
    let onDone: (RepeatingDbPeriodDaysOfYear.MonthDayItem) -> Void
    
    ///
    
    @Environment(\.dismiss) private var dismiss

    @State private var monthId: Int32 = 1
    @State private var dayId: Int32 = 1
    @State private var dayIds: [Int32] = Array(1...5)
    
    var body: some View {
        Form {
            
            Picker("Month", selection: $monthId) {
                ForEach(RepeatingDbPeriodDaysOfYear.companion.months, id: \.id) { month in
                    Text(month.getName())
                        .tag(month.id)
                }
            }
            .foregroundColor(.primary)
            .onChange(of: monthId) { _, newMonthId in
                withAnimation {
                    dayId = 1
                }
                upPickerDays()
            }
            
            Picker("Day", selection: $dayId) {
                ForEach(dayIds, id: \.self) {
                    Text("\($0)")
                }
            }
            .foregroundColor(.primary)
        }
        .myFormContentMargins()
        .interactiveDismissDisabled()
        .navigationTitle("Day of the Year")
        .toolbarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .cancellationAction) {
                Button("Cancel") {
                    dismiss()
                }
            }
            ToolbarItem(placement: .primaryAction) {
                Button("Add") {
                    onDone(
                        RepeatingDbPeriodDaysOfYear.MonthDayItem(
                            monthId: monthId,
                            dayId: dayId
                        )
                    )
                    dismiss()
                }
                .fontWeight(.semibold)
            }
        }
        .onAppear {
            upPickerDays()
        }
    }
    
    private func upPickerDays() {
        let month = RepeatingDbPeriodDaysOfYear.companion.months.first {
            $0.id == monthId
        }!
        dayIds = Array(month.days.first...month.days.last)
    }
}
