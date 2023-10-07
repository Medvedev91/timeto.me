import SwiftUI
import shared

struct SummarySheet: View {

    @Binding var isPresented: Bool

    ///

    @State private var vm = SummarySheetVM()

    var body: some View {

        VMView(vm: vm, stack: .VStack()) { state in

            if state.isChartVisible {
                ChartView(activitiesUI: state.activitiesUI)
                        .id(state)
            }

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

                    HStack {

                        Button(
                                action: {
                                    vm.toggleIsChartVisible()
                                },
                                label: {
                                    HStack {
                                        Image(systemName: "chart.pie")
                                                .font(.system(size: 22, weight: .light))
                                                .foregroundColor(state.isChartVisible ? c.white : c.textSecondary)
                                                .padding(2)
                                    }
                                }
                        )
                                .background(roundedShape.fill(state.isChartVisible ? c.blue : c.transparent ))

                        Spacer()

                        DatePickerStateView(
                                unixTime: state.pickerTimeStart,
                                minTime: state.minPickerTime,
                                maxTime: state.maxPickerTime
                        ) { newTime in
                            vm.setPickerTimeStart(unixTime: newTime)
                        }
                                .labelsHidden()

                        Text("-")
                                .padding(.horizontal, 6)

                        DatePickerStateView(
                                unixTime: state.pickerTimeFinish,
                                minTime: state.minPickerTime,
                                maxTime: state.maxPickerTime
                        ) { newTime in
                            vm.setPickerTimeFinish(unixTime: newTime)
                        }
                                .labelsHidden()

                        Spacer()

                        Button(
                                action: {
                                    vm.toggleIsChartVisible()
                                },
                                label: {
                                    HStack {
                                        Image(systemName: "xmark")
                                                .font(.system(size: 20, weight: .light))
                                                .foregroundColor(c.textSecondary)
                                    }
                                }
                        )
                    }
                            .padding(.top, 10)
                            .padding(.horizontal, 16)
                }
            }
        }
    }
}
