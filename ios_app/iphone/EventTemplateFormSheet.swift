import SwiftUI
import shared

struct EventTemplateFormSheet: View {
    
    @Binding private var isPresented: Bool
    
    @State private var vm: EventTemplateFormSheetVm
    
    @EnvironmentObject private var nativeSheet: NativeSheet
    
    @State private var scroll = 0
    
    init(
        isPresented: Binding<Bool>,
        eventTemplateDb: EventTemplateDb?
    ) {
        _isPresented = isPresented
        _vm = State(initialValue: EventTemplateFormSheetVm(
            eventTemplateDB: eventTemplateDb
        ))
    }
    
    var body: some View {
        
        VMView(vm: vm, stack: .VStack()) { state in
            
            Sheet__HeaderView(
                title: state.headerTitle,
                scrollToHeader: scroll,
                bgColor: c.sheetBg
            )
            
            ScrollViewWithVListener(showsIndicators: false, vScroll: $scroll) {
                
                VStack {
                    
                    MyListView__ItemView(
                        isFirst: true,
                        isLast: true
                    ) {
                        
                        MyListView__ItemView__TextInputView(
                            text: state.inputTextValue,
                            placeholder: "Text",
                            isAutofocus: false,
                            onValueChanged: { newText in vm.setInputTextValue(text: newText) }
                        )
                    }
                    .padding(.top, 12)
                    
                    MyListView__Padding__SectionSection()
                    
                    MyListView__ItemView(
                        isFirst: true,
                        isLast: true
                    ) {
                        
                        MyListView__ItemView__ButtonView(
                            text: state.daytimeTitle,
                            withArrow: true,
                            rightView: AnyView(
                                MyListView__ItemView__ButtonView__RightText(
                                    text: state.daytimeNote,
                                    paddingEnd: 2,
                                    textColor: state.daytimeNoteColor?.toColor()
                                )
                            )
                        ) {
                            nativeSheet.show { isTimerPickerPresented in
                                DaytimePickerSheet(
                                    isPresented: isTimerPickerPresented,
                                    title: state.daytimeTitle,
                                    doneText: "Done",
                                    daytimeUi: state.defDaytimeUi,
                                    onPick: { daytimeUi in
                                        vm.setDaytime(daytimeUi: daytimeUi)
                                    },
                                    onRemove: {
                                        vm.setDaytime(daytimeUi: nil)
                                    }
                                )
                                .presentationDetents([.medium])
                                .presentationDragIndicator(.visible)
                            }
                        }
                    }
                    
                    MyListView__Padding__SectionSection()
                    
                    TextFeaturesTimerFormView(
                        textFeatures: state.textFeatures
                    ) { textFeatures in
                        vm.setTextFeatures(newTextFeatures: textFeatures)
                    }
                    
                    MyListView__Padding__SectionSection()
                    
                    TextFeaturesTriggersFormView(
                        textFeatures: state.textFeatures
                    ) { textFeatures in
                        vm.setTextFeatures(newTextFeatures: textFeatures)
                    }
                    
                    if let eventTemplate = vm.eventTemplateDB {
                        
                        MyListView__Padding__SectionSection()
                        
                        MyListView__ItemView(
                            isFirst: true,
                            isLast: true
                        ) {
                            MyListView__ItemView__ActionView(
                                text: state.deleteText
                            ) {
                                vm.delete(templateDB: eventTemplate) {
                                    isPresented = false
                                }
                            }
                        }
                    }
                }
            }
            
            Sheet__BottomViewDefault(
                primaryText: state.doneText,
                primaryAction: {
                    vm.save {
                        isPresented = false
                    }
                },
                secondaryText: "Cancel",
                secondaryAction: {
                    isPresented = false
                },
                topContent: { EmptyView() },
                startContent: { EmptyView() }
            )
        }
    }
}
