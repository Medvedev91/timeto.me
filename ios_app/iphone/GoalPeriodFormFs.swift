import SwiftUI
import shared

struct GoalPeriodFormFs: View {
    
    @State private var vm: GoalPeriodFormVm
    @Binding private var isPresented: Bool
    private let onSelect: (GoalDbPeriod) -> ()
    
    @State private var fsHeaderScroll = 0

    init(
        isPresented: Binding<Bool>,
        initPeriod: GoalDbPeriod?,
        onSelect: @escaping (GoalDbPeriod) -> ()
    ) {
        _isPresented = isPresented
        self.onSelect = onSelect
        vm = GoalPeriodFormVm(initPeriod: initPeriod)
    }
    
    var body: some View {
        
        VMView(vm: vm, stack: .VStack()) { state in
            
            Fs__HeaderAction(
                title: state.headerTitle,
                actionText: state.headerDoneText,
                scrollToHeader: fsHeaderScroll,
                onCancel: {
                    isPresented = false
                },
                onDone: {
                    vm.buildPeriod { period in
                        onSelect(period)
                        isPresented = false
                    }
                }
            )
            
            ScrollViewWithVListener(showsIndicators: false, vScroll: $fsHeaderScroll) {
                
                VStack {
                    
                    MyListView__PaddingFirst()
                    
                    MyListView__ItemView(
                        isFirst: true,
                        isLast: true,
                        bgColor: c.fg
                    ) {
                        
                        VStack {
                            
                            MyListView__ItemView__RadioView(
                                text: state.daysOfWeekTitle,
                                isActive: state.isDaysOfWeekSelected,
                                onClick: {
                                    vm.setTypeDaysOfWeek()
                                }
                            )
                            
                            if state.isDaysOfWeekSelected {
                                WeekDaysFormView(
                                    weekDays: state.daysOfWeek.map { $0.toInt().toKotlinInt() },
                                    size: 36,
                                    onChange: { newWeekDays in
                                        vm.setDaysOfWeek(daysOfWeek: Set(newWeekDays))
                                    }
                                )
                                .padding(.leading, H_PADDING)
                                .padding(.top, 4)
                                .padding(.bottom, 16)
                            }
                        }
                    }
                    
                    MyListView__Padding__SectionSection()
                    
                    MyListView__ItemView(
                        isFirst: true,
                        isLast: true,
                        bgColor: c.fg
                    ) {
                        
                        MyListView__ItemView__RadioView(
                            text: state.weeklyTitle,
                            isActive: state.isWeeklySelected,
                            onClick: {
                                vm.setTypeWeekly()
                            }
                        )
                    }
                }
            }
        }
    }
}
