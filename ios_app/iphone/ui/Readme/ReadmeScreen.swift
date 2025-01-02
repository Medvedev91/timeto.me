import SwiftUI
import shared

private let pTextLineHeight = 3.2

struct ReadmeScreen: View {
    
    let defaultItem: ReadmeSheetVm.DefaultItem
    
    var body: some View {
        VmView({
            ReadmeSheetVm(defaultItem: defaultItem)
        }) { vm, state in
            ReadmeScreenInner(
                vm: vm,
                state: state,
                selectedTab: state.tabUi // @State inited once
            )
        }
    }
}

private struct ReadmeScreenInner: View {
    
    let vm: ReadmeSheetVm
    let state: ReadmeSheetVm.State
    
    @State var selectedTab: ReadmeSheetVm.TabUi
    
    var body: some View {
        
        ZStack {
            TabView(tabUi: state.tabUi)
                .id("tab_\(state.tabUi.id)") // To scroll to top
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .principal) {
                Picker("", selection: $selectedTab) {
                    ForEach(state.tabsUi, id: \.id) { tabUi in
                        Text(tabUi.title)
                            .tag(tabUi)
                    }
                }
                .pickerStyle(.segmented)
            }
        }
        .onChange(of: selectedTab) { _, new in
            vm.setTabUi(tabUi: new)
        }
    }
}

///

private struct TabView: View {
    
    let tabUi: ReadmeSheetVm.TabUi
    
    var body: some View {
        
        List {
            
            let paragraphs = tabUi.paragraphs
            
            ForEachIndexed(paragraphs) { idx, paragraph in
                
                let prevP: ReadmeSheetVm.Paragraph? = (idx == 0) ? nil : paragraphs[idx - 1]
                
                ZStack {
                    
                    if let paragraph = paragraph as? ReadmeSheetVm.ParagraphTitle {
                        let paddingTop: CGFloat = {
                            guard let prevP = prevP else {
                                return 12
                            }
                            if prevP.isSlider {
                                return 40
                            }
                            if prevP is ReadmeSheetVm.ParagraphText {
                                return 39
                            }
                            fatalError()
                        }()
                        HStack {
                            Text(paragraph.text)
                                .foregroundColor(.primary)
                                .font(.system(size: Fs__TITLE_FONT_SIZE, weight: Fs__TITLE_FONT_WEIGHT))
                                .padding(.top, paddingTop)
                                .padding(.horizontal, H_PADDING)
                            Spacer()
                        }
                    } else if let paragraph = paragraph as? ReadmeSheetVm.ParagraphText {
                        let paddingTop: CGFloat = {
                            guard let prevP = prevP else {
                                return 14
                            }
                            if prevP.isSlider {
                                return 16
                            }
                            if prevP is ReadmeSheetVm.ParagraphTitle {
                                return 17
                            }
                            if prevP is ReadmeSheetVm.ParagraphText {
                                return 16
                            }
                            if prevP is ReadmeSheetVm.ParagraphTextHighlight {
                                return 20
                            }
                            fatalError()
                        }()
                        HStack {
                            Text(paragraph.text)
                                .foregroundColor(.primary)
                                .padding(.top, paddingTop)
                                .padding(.horizontal, H_PADDING)
                                .lineSpacing(pTextLineHeight)
                            Spacer()
                        }
                    } else if let paragraph = paragraph as? ReadmeSheetVm.ParagraphTextHighlight {
                        let paddingTop: CGFloat = {
                            guard let prevP = prevP else {
                                fatalError()
                            }
                            if prevP.isSlider {
                                return 24
                            }
                            if prevP is ReadmeSheetVm.ParagraphText {
                                return 23
                            }
                            fatalError()
                        }()
                        HStack {
                            Text(paragraph.text)
                                .foregroundColor(.primary)
                                .padding(.vertical, 13)
                                .padding(.horizontal, 14)
                                .lineSpacing(pTextLineHeight)
                            Spacer()
                        }
                        .background(squircleShape.fill(.blue))
                        .padding(.top, paddingTop)
                        .padding(.horizontal, H_PADDING - 2)
                    } else if let paragraph = paragraph as? ReadmeSheetVm.ParagraphAskAQuestion {
                        AskQuestion(
                            subject: paragraph.subject
                        ) {
                            Text(paragraph.title)
                                .foregroundColor(.blue)
                        }
                        .padding(.top, 24)
                        .padding(.leading, H_PADDING)
                    } else if paragraph is ReadmeSheetVm.ParagraphTimerTypical {
                        ReadmeImagesPreview(
                            images: [
                                "readme_timer_1"
                            ]
                        )
                    } else if paragraph is ReadmeSheetVm.ParagraphTimerMyActivities {
                        ReadmeImagesPreview(
                            images: [
                                "readme_activities_1"
                            ]
                        )
                    } else if paragraph is ReadmeSheetVm.ParagraphTimerCharts {
                        ReadmeImagesPreview(
                            images: [
                                "readme_chart_1",
                                "readme_chart_2",
                                "readme_chart_3"
                            ]
                        )
                    } else if paragraph is ReadmeSheetVm.ParagraphTimerPractice1 {
                        ReadmeImagesPreview(
                            images: [
                                "readme_timer_practice_1",
                                "readme_timer_practice_2",
                                "readme_timer_practice_3",
                                "readme_timer_practice_4"
                            ]
                        )
                    } else if paragraph is ReadmeSheetVm.ParagraphTimerPractice2 {
                        ReadmeImagesPreview(
                            images: [
                                "readme_timer_practice_5",
                                "readme_chart_2",
                                "readme_chart_3"
                            ]
                        )
                    } else if paragraph is ReadmeSheetVm.ParagraphRepeatingsMy {
                        ReadmeImagesPreview(
                            images: [
                                "readme_repeatings_1"
                            ]
                        )
                    } else if paragraph is ReadmeSheetVm.ParagraphRepeatingsToday {
                        ReadmeImagesPreview(
                            images: [
                                "readme_repeatings_2"
                            ]
                        )
                    } else if paragraph is ReadmeSheetVm.ParagraphRepeatingsPractice1 {
                        ReadmeImagesPreview(
                            images: [
                                "readme_repeating_practice_1",
                                "readme_repeating_practice_2",
                                "readme_repeating_practice_3"
                            ]
                        )
                    } else if paragraph is ReadmeSheetVm.ParagraphRepeatingsPractice2 {
                        ReadmeImagesPreview(
                            images: [
                                "readme_repeating_practice_4",
                                "readme_repeating_practice_5"
                            ]
                        )
                    } else if paragraph is ReadmeSheetVm.ParagraphChecklistsExamples {
                        ReadmeImagesPreview(
                            images: [
                                "readme_checklists_1",
                                "readme_checklists_2",
                                "readme_checklists_3"
                            ]
                        )
                    } else if paragraph is ReadmeSheetVm.ParagraphChecklistsPractice1 {
                        ReadmeImagesPreview(
                            images: [
                                "readme_checklists_practice_1",
                                "readme_checklists_practice_2",
                                "readme_checklists_practice_3",
                                "readme_checklists_practice_4",
                                "readme_checklists_practice_5",
                                "readme_checklists_practice_6",
                                "readme_checklists_practice_7"
                            ]
                        )
                    } else if paragraph is ReadmeSheetVm.ParagraphChecklistsPractice2 {
                        ReadmeImagesPreview(
                            images: [
                                "readme_checklists_practice_8",
                                "readme_checklists_practice_9"
                            ]
                        )
                    } else if paragraph is ReadmeSheetVm.ParagraphPomodoroExamples {
                        ReadmeImagesPreview(
                            images: [
                                "readme_pomodoro_1",
                                "readme_pomodoro_2",
                                "readme_pomodoro_3",
                            ]
                        )
                    } else if paragraph is ReadmeSheetVm.ParagraphGoalsExamples {
                        ReadmeImagesPreview(
                            images: [
                                "readme_goals_1"
                            ]
                        )
                    } else if paragraph is ReadmeSheetVm.ParagraphCalendarExamples {
                        ReadmeImagesPreview(
                            images: [
                                "readme_calendar_1",
                                "readme_calendar_2"
                            ]
                        )
                    } else {
                        fatalError()
                    }
                }
            }
            .plainListItem()
            
            ZStack {}
                .padding(.top, 28)
                .plainListItem()
        }
        .plainList()
    }
}
