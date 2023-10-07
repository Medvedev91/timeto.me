import SwiftUI
import shared

struct SummarySheet: View {

    @Binding var isPresented: Bool

    ///

    @State private var vm = SummarySheetVM()

    var body: some View {

        VMView(vm: vm, stack: .VStack()) { state in

            Spacer()

            Sheet__BottomView {

                VStack {

                    HStack {

                        ForEachIndexed(state.periodHints) { _, period in

                            Button(
                                    action: {
                                        vm.setPeriod(
                                                pickerTimeStart: period.pickerTimeStart,
                                                pickerTimeFinish: period.pickerTimeFinish
                                        )
                                    },
                                    label: {
                                        Text(period.title)
                                                .font(.system(size: 14, weight: period.isActive ? .bold : .light))
                                                .foregroundColor(period.isActive ? c.white : c.text)
                                                .padding(.horizontal, 8)
                                                .padding(.vertical, 6)
                                    }
                            )
                        }
                    }
                            .padding(.top, 12)

                    HStack(spacing: 6) {

                        DatePickerStateView(
                                unixTime: state.pickerTimeStart,
                                minTime: state.minPickerTime,
                                maxTime: state.maxPickerTime
                        ) { newTime in
                            vm.setPickerTimeStart(unixTime: newTime)
                        }
                                .labelsHidden()

                        Text("-")

                        DatePickerStateView(
                                unixTime: state.pickerTimeFinish,
                                minTime: state.minPickerTime,
                                maxTime: state.maxPickerTime
                        ) { newTime in
                            vm.setPickerTimeFinish(unixTime: newTime)
                        }
                                .labelsHidden()
                    }
                            .padding(.top, 10)
                }
            }
        }
    }
}
